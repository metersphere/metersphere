package io.metersphere.novice.controller;

import io.metersphere.base.domain.NoviceStatistics;
import io.metersphere.novice.request.NoviceRequest;
import io.metersphere.novice.service.NoviceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: LAN
 * @date: 2023/3/17 14:07
 * @version: 1.0
 */
@RestController
@RequestMapping(value = "novice")
public class NoviceController {
    @Resource
    private NoviceService noviceService;

    @PostMapping("/info")
    public List<NoviceStatistics> getNoviceInfo() {
        return noviceService.getNoviceInfo();
    }

    @PostMapping("/save/step")
    public void saveStep(@RequestBody NoviceRequest noviceRequest) {
        noviceService.saveStep(noviceRequest);
    }

    @PostMapping("/save/task")
    public void saveTask(@RequestBody NoviceStatistics noviceStatistics) {
        noviceService.saveNoviceInfo(noviceStatistics);
    }
    @PostMapping("/status")
    public void updateStatus(@RequestBody NoviceRequest noviceRequest) {
        noviceService.updateStatus(noviceRequest);
    }
}
