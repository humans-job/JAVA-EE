package edu.service;

import edu.dto.militia.MilitiaAuditReq;
import edu.dto.militia.MilitiaBatchImportReq;
import edu.dto.militia.MilitiaSubmitAuditReq;
import edu.dto.militia.MilitiaUpdateReq;
import edu.vo.militia.MilitiaImportResp;
import edu.vo.militia.MilitiaListResp;
import org.example.army.militarycommon.Entity.militiaInfo;

public interface MilitiaArchiveService {

    MilitiaImportResp batchImport(MilitiaBatchImportReq req);

    void submitToDivision(MilitiaSubmitAuditReq req);

    void divisionAudit(MilitiaAuditReq req);

    MilitiaListResp list(Long deptId, Integer auditStatus, String idCardLike, long pageNum, long pageSize);

    Long update(MilitiaUpdateReq update);

    void delete(Long id);
}
