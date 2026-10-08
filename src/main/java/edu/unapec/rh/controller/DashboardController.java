package edu.unapec.rh.controller;

import edu.unapec.rh.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {
  private final DashboardService dashboard;

  @GetMapping("/")
  public String index(Model model) {
    model.addAllAttributes(dashboard.datos());
    model.addAttribute("title", "Dashboard");
    return "dashboard/index";
  }
}
