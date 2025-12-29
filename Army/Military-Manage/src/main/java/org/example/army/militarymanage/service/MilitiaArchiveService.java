package org.example.army.militarymanage.service;

import org.example.army.militarymanage.dto.militia.MilitiaAuditReq;
import org.example.army.militarymanage.dto.militia.MilitiaBatchImportReq;
import org.example.army.militarymanage.dto.militia.MilitiaSubmitAuditReq;
import org.example.army.militarymanage.dto.militia.MilitiaUpdateReq;
import org.example.army.militarymanage.vo.militia.MilitiaImportResp;
import org.example.army.militarymanage.vo.militia.MilitiaListResp;

public interface MilitiaArchiveService {

    MilitiaImportResp batchImport(MilitiaBatchImportReq req);

    void submitToDivision(MilitiaSubmitAuditReq req);

    void divisionAudit(MilitiaAuditReq req);

    MilitiaListResp list(Long deptId, Integer auditStatus, String idCardLike, long pageNum, long pageSize);

    Long update(MilitiaUpdateReq update);

    void delete(Long id);
}
