package org.example.army.militaryauthenticate.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.army.militarycommon.Entity.User;
import org.example.army.militarycommon.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Component
public class PkiServiceUtil{

    private final CaStore caStore;
    private final UserMapper userMapper;

    @Value("${pki.ca.subject-dn}")
    private String caSubjectDn;

    @Value("${pki.ca.valid-days}")
    private int caValidDays;

    @Value("${pki.user.valid-days}")
    private int userValidDays;

    public PkiServiceUtil(CaStore caStore, UserMapper userMapper) {
        this.caStore = caStore;
        this.userMapper = userMapper;
    }

    /** 初始化 CA（只需一次） */
    @Transactional
    public String initCaIfAbsent() {
        if (caStore.exists()) return "CA已存在";
        KeyPair caKeyPair = PkiUtil.genRsaKeyPair(4096);
        X509Certificate caCert = PkiUtil.createCaCert(caKeyPair, caSubjectDn, caValidDays);
        caStore.save(caKeyPair.getPrivate(), caCert);
        return "CA初始化完成";
    }

    public record IssueResult(String certSn, String certPem, String p12Base64, String p12Password) {}

    /** 核心：加载/找到CA -> 用CA私钥签发用户证书 -> 写入sys_user.cert_sn */
    @Transactional
    public IssueResult issueCertForUser(Long userId) {
        initCaIfAbsent();
        CaStore.LoadedCa ca = caStore.load(); // ✅ “找CA”就在这里：读 CA 私钥+证书

        User user = userMapper.selectById(userId);
        if (user == null) throw new RuntimeException("用户不存在");
        if (user.getStatus() != null && user.getStatus() != 1) throw new RuntimeException("用户状态异常");

        // 1) 生成用户KeyPair（也可改成客户端生成CSR，服务端只签）
        KeyPair userKeyPair = PkiUtil.genRsaKeyPair(2048);

        // 2) 生成唯一序列号（写入 cert_sn）
        BigInteger serial = nextUniqueSerial();
        String certSnHex = PkiUtil.serialHex(serial);

        // 3) 用户证书DN（按你实际需求定制）
        String userDn = "CN=" + user.getUsername() + ", OU=Users, O=YourOrg, C=CN";

        // 4) CA签发用户证书（issuer=CA）
        X509Certificate userCert = PkiUtil.issueUserCert(
                userKeyPair.getPublic(),
                userDn,
                ca.privateKey(),
                ca.certificate(),
                serial,
                userValidDays
        );

        // 5) 导出给用户：p12（含私钥+用户证书+CA链）
        String p12Pwd = randomPassword(10);
        byte[] p12 = PkiUtil.toPkcs12("user", userKeyPair.getPrivate(),
                new X509Certificate[]{userCert, ca.certificate()}, p12Pwd);

        // 6) 写库：sys_user.cert_sn
        User upd = new User();
        upd.setUserId(userId);
        upd.setCertSn(certSnHex);
        userMapper.updateById(upd);

        return new IssueResult(
                certSnHex,
                PkiUtil.certToPem(userCert),
                Base64.getEncoder().encodeToString(p12),
                p12Pwd
        );
    }

    private BigInteger nextUniqueSerial() {
        SecureRandom r = new SecureRandom();
        for (int i = 0; i < 30; i++) {
            BigInteger serial = new BigInteger(128, r).abs();
            String hex = PkiUtil.serialHex(serial);
            Long cnt = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getCertSn, hex));
            if (cnt == 0) return serial;
        }
        throw new RuntimeException("生成唯一证书序列号失败（重复过多）");
    }

    private String randomPassword(int len) {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }
}

