package de.astahsrm.gremiomat.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    @GetMapping
    public String getAdminPage() {
        return "mgmt/admin/admin";
    }

    @GetMapping("/csv-user-upload")
    public String getCSVPage() {
        return "mgmt/admin/csv-user-upload";
    }
    
    @PostMapping("/csv-user-upload")
    public String processUserCSV() {
        return "";
    }

    @GetMapping("/gremien")
    public String getGremienOverview() {
        return "";
    }

    @GetMapping("/gremien/new")
    public String getNewGremiumEditPage() {
        return "";
    }

    @PostMapping("/gremien/new")
    public String postNewUserGremiumPage() {
        return "redirect:/admin/gremien";
    }
    
    @GetMapping("/gremien/{abbr}")
    public String getGremiumInfoPage(@PathVariable String abbr) {
        return "";
    }

    @GetMapping("/gremien/{abbr}/edit")
    public String getGremiumEditPage(@PathVariable String abbr) {
        return "";
    }

    @PostMapping("/gremien/{abbr}/edit")
    public String postGremiumEditPage(@PathVariable String abbr) {
        return "";
    }

    @GetMapping("/gremien/{abbr}/{queryIndex}/edit")
    public String getGremiumQueryEditPage(@PathVariable String abbr, @PathVariable int queryIndex) {
        return "";
    }

    @PostMapping("/gremien/{abbr}/{queryIndex}/edit")
    public String postGremiumQueryEditPage(@PathVariable String abbr, @PathVariable int queryIndex) {
        return "";
    }

    @GetMapping("/users")
    public String getUserOverview() {
        return "";
    }
    
    @GetMapping("/users/new")
    public String getNewUserEditPage() {
        return "";
    }

    @PostMapping("/users/new")
    public String postNewUserEditPage() {
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{username}")
    public String getUserInfoPage(@PathVariable String username) {
        return "";
    }

    @GetMapping("/gremien/{username}/edit")
    public String getUserEditPage(@PathVariable String username) {
        return "";
    }

    @PostMapping("/gremien/{username}/edit")
    public String postUserEditPage(@PathVariable String username) {
        return "redirect:/admin/gremien/" + username;
    }

}
