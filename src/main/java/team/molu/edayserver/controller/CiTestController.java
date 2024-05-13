package team.molu.edayserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CiTestController {

    @GetMapping("/test")
    public String TestApi() {
        return "api connection is complete.";
    }
}
