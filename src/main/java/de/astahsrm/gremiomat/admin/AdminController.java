package de.astahsrm.gremiomat.admin;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.opencsv.exceptions.CsvException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import de.astahsrm.gremiomat.candidate.Candidate;
import de.astahsrm.gremiomat.candidate.CandidateDtoAdmin;
import de.astahsrm.gremiomat.candidate.CandidateService;
import de.astahsrm.gremiomat.gremium.Gremium;
import de.astahsrm.gremiomat.gremium.GremiumDto;
import de.astahsrm.gremiomat.gremium.GremiumService;
import de.astahsrm.gremiomat.mgmt.MgmtUser;
import de.astahsrm.gremiomat.mgmt.MgmtUserService;
import de.astahsrm.gremiomat.photo.Photo;
import de.astahsrm.gremiomat.photo.PhotoService;
import de.astahsrm.gremiomat.query.Query;
import de.astahsrm.gremiomat.query.QueryAdminDto;
import de.astahsrm.gremiomat.query.QueryService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private GremiumService gremiumService;

    @Autowired
    private QueryService queryService;

    @Autowired
    private MgmtUserService mgmtUserService;

    @Autowired
    private PhotoService photoService;

    @GetMapping
    public String getAdminPage() {
        return "admin/admin";
    }

    @PostMapping("/csv-user-upload")
    public String processUserCSV(@RequestParam("csv-file") MultipartFile csvFile, HttpServletRequest req,
            @RequestParam("gremiumSelect") String gremiumAbbr) throws IOException, CsvException {
        candidateService.saveCandidatesFromCSV(csvFile, gremiumAbbr, req.getLocale());
        return "redirect:/admin/candidates";
    }

    @PostMapping("/csv-query-upload")
    public String processQueryCSV(@RequestParam("csv-file") MultipartFile csvFile,
            @RequestParam("gremiumSelect") String abbr) throws IOException, CsvException {
        queryService.saveQueriesFromCSV(csvFile, abbr);
        return "redirect:/admin/gremien/" + abbr;
    }

    @GetMapping("/gremien")
    public String getGremien(Model m) {
        m.addAllAttributes(gremiumService.getGremienNavMap());
        m.addAttribute("form", new GremiumDto());
        return "admin/gremien";
    }

    @PostMapping("/gremien/new")
    public String saveNewGremiumString(GremiumDto form, BindingResult res, Model m) {
        if (res.hasErrors()) {
            m.addAttribute("errors", res.getAllErrors());
            return "error";
        }
        Gremium gremium = new Gremium();
        gremium.setName(form.getName());
        gremium.setAbbr(form.getAbbr());
        gremium.setDescription(form.getDescription());
        gremiumService.saveGremium(gremium);
        return "redirect:/admin/gremien";
    }

    @GetMapping("/gremien/{abbr}")
    public String getGremiumInfoPage(@PathVariable String abbr, Model m) {
        Optional<Gremium> gOpt = gremiumService.findGremiumByAbbr(abbr);
        if (gOpt.isPresent()) {
            Gremium g = gOpt.get();
            GremiumDto form = new GremiumDto();
            form.setAbbr(g.getAbbr());
            form.setName(g.getName());
            form.setDescription(g.getDescription());
            m.addAttribute("form", form);
            m.addAttribute("gremium", g);
            return "admin/gremium";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, GremiumService.GREMIUM_NOT_FOUND);
    }

    @PostMapping("/gremien/{abbr}")
    public String postGremiumEditPage(GremiumDto form, BindingResult res, Model m) {
        if (res.hasErrors()) {
            m.addAttribute("errors", res.getAllErrors());
            return "error";
        }
        Optional<Gremium> gremiumOptional = gremiumService.findGremiumByAbbr(form.getAbbr());
        if (gremiumOptional.isPresent()) {
            Gremium gremium = gremiumOptional.get();
            gremium.setName(form.getName());
            gremium.setDescription(form.getDescription());
            return "redirect:/admin/gremien/" + gremiumService.saveGremium(gremium).getAbbr();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, GremiumService.GREMIUM_NOT_FOUND);
    }

    @GetMapping("/gremien/{abbr}/queries/new")
    public String newQuery(@PathVariable String abbr, Model m) {
        m.addAttribute("allGremien", gremiumService.getAllGremiumsSortedByName());
        m.addAttribute("form", new QueryAdminDto());
        m.addAttribute("abbr", abbr);
        return "admin/query";
    }

    @PostMapping("/gremien/{abbr}/queries/new")
    public String saveNewQuery(@PathVariable String abbr, QueryAdminDto form, BindingResult res, Model m) {
        if (res.hasErrors()) {
            m.addAttribute("error", res.getAllErrors());
            return "admin/query-edit";
        } else {
            form.addGremiumAbbr(abbr);
            updateQuery(new Query(), form);
            return "redirect:/admin/gremien/" + abbr;
        }
    }

    @GetMapping("/gremien/{abbr}/queries/{id}")
    public String getGremiumQueryEditPage(@PathVariable String abbr, @PathVariable long id, Model m) {
        Optional<Query> qOpt = queryService.getQueryById(id);
        if (qOpt.isPresent()) {
            Query query = qOpt.get();
            QueryAdminDto form = new QueryAdminDto();
            form.setTxt(query.getText());
            for (Gremium g : query.getGremien()) {
                form.addGremium(g);
            }
            m.addAttribute("allGremien", gremiumService.getAllGremiumsSortedByName());
            m.addAttribute("form", form);
            m.addAttribute("query", query);
            m.addAttribute("abbr", abbr);
            return "admin/query";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, QueryService.QUERY_NOT_FOUND);
    }

    @PostMapping(value = "/gremien/{abbr}/queries/{id}")
    public String postSaveGremiumQueryEditPage(@PathVariable String abbr, @PathVariable long id, QueryAdminDto form,
            BindingResult res, Model m) {
        if (res.hasErrors()) {
            m.addAttribute("error", res.getAllErrors());
            return "admin/query-edit";
        } else {
            Optional<Query> qOpt = queryService.getQueryById(id);
            if (qOpt.isPresent()) {
                updateQuery(qOpt.get(), form);
                return "redirect:/admin/gremien/" + abbr;
            }
            return "error";
        }
    }

    @GetMapping("/gremien/{abbr}/queries/{id}/delete")
    public String deleteQuery(@PathVariable String abbr, @PathVariable long id) {
        queryService.delQueryById(id);
        return "redirect:/admin/gremien/" + abbr;
    }

    @GetMapping("/users")
    public String getUserOverview(Model m) {
        m.addAttribute("allUsers", mgmtUserService.getAllUsersSortedByUsername());
        return "admin/users";
    }

    @GetMapping("/users/new")
    public String getUserNew(Principal loggedInUser, Model m) {
        Candidate c = new Candidate();
        CandidateDtoAdmin form = new CandidateDtoAdmin();
        form.setAge(c.getAge());
        form.setBio(c.getBio());
        form.setCourse(c.getCourse());
        form.setEmail(c.getEmail());
        form.setFirstname(c.getFirstname());
        form.setGremien(c.getGremien());
        form.setLastname(c.getLastname());
        form.setSemester(c.getSemester());
        m.addAttribute("username", loggedInUser.getName());
        m.addAttribute("allGremien", gremiumService.getAllGremiumsSortedByName());
        m.addAttribute("form", form);
        return "admin/user-edit";
    }

    @PostMapping("/users/new")
    public String postNewUser(HttpServletRequest request, @ModelAttribute CandidateDtoAdmin form, BindingResult res,
            Model m) {
        if (res.hasErrors()) {
            return "admin/user-edit";
        }
        Candidate c = new Candidate();
        c.setFirstname(form.getFirstname());
        c.setLastname(form.getLastname());
        c.setAge(form.getAge());
        c.setSemester(form.getSemester());
        c.setCourse(form.getCourse());
        c.setBio(form.getBio());
        c.setEmail(form.getEmail());
        c.setGremien(form.getGremien());
        if (!candidateService.candidateExists(c)) {
            mgmtUserService.saveNewUser(c, request.getLocale());
            return "redirect:/admin/users/";
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists!");
        }
    }

    @GetMapping("/users/lock")
    public String lockUser(Model m) {
        mgmtUserService.lockAllUsers();
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{username}")
    public String getUserInfoPage(@PathVariable String username, Model m) {
        m.addAttribute("candidate", mgmtUserService.getCandidateDetailsOfUser(username));
        m.addAttribute("username", username);
        return "user/user-info";
    }

    @GetMapping("/users/{username}/edit")
    public String getUserEditPage(@PathVariable String username, Model m) {
        Candidate c = mgmtUserService.getCandidateDetailsOfUser(username);
        CandidateDtoAdmin form = new CandidateDtoAdmin();
        form.setAge(c.getAge());
        form.setBio(c.getBio());
        form.setCourse(c.getCourse());
        form.setEmail(c.getEmail());
        form.setFirstname(c.getFirstname());
        form.setGremien(c.getGremien());
        form.setLastname(c.getLastname());
        form.setSemester(c.getSemester());
        if (c.getPhoto() != null) {
            m.addAttribute("photoId", c.getPhoto().getId());
        }
        m.addAttribute("username", username);
        m.addAttribute("allGremien", gremiumService.getAllGremiumsSortedByName());
        m.addAttribute("form", form);
        return "admin/user-edit";
    }

    @PostMapping(value = "/users/{username}/edit", params = "del")
    public String postUserEditDel(@PathVariable String username, Model m) {
        mgmtUserService.delUserById(username);
        return "redirect:/admin/users";
    }

    @PostMapping(value = "/users/{username}/edit", params = "save")
    public String postUserEditPage(@PathVariable String username, @ModelAttribute CandidateDtoAdmin form,
            BindingResult res, Model m) {
        if (res.hasErrors()) {
            return "admin/user-edit";
        }
        Candidate c = mgmtUserService.getCandidateDetailsOfUser(username);
        c.setFirstname(form.getFirstname());
        c.setLastname(form.getLastname());
        c.setAge(form.getAge());
        c.setSemester(form.getSemester());
        c.setCourse(form.getCourse());
        c.setBio(form.getBio());
        c.setEmail(form.getEmail());
        c.setGremien(form.getGremien());
        Optional<MgmtUser> uOpt = mgmtUserService.getUserById(username);
        if (uOpt.isPresent()) {
            MgmtUser u = uOpt.get();
            u.setDetails(candidateService.saveCandidate(c));
            mgmtUserService.saveUser(u);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MgmtUserService.USER_NOT_FOUND);
        }
        return "redirect:/admin/users/" + username;
    }

    @GetMapping("/users/{username}/upload/del")
    public String getUserInfoUploadDel(@PathVariable String username, Model m) {
        Candidate c = mgmtUserService.getCandidateDetailsOfUser(username);
        Photo p = c.getPhoto();
        c.setPhoto(null);
        photoService.delPhoto(p);
        Optional<MgmtUser> uOpt = mgmtUserService.getUserById(username);
        if (uOpt.isPresent()) {
            MgmtUser u = uOpt.get();
            u.setDetails(candidateService.saveCandidate(c));
            mgmtUserService.saveUser(u);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MgmtUserService.USER_NOT_FOUND);
        }
        return "redirect:/admin/users/" + username + "/edit";
    }

    @GetMapping("/users/{username}/upload")
    public String getUserInfoUpload(@PathVariable String username, Model m) {
        Candidate userDetails = mgmtUserService.getCandidateDetailsOfUser(username);
        if (userDetails.getPhoto() != null) {
            m.addAttribute("photoId", userDetails.getPhoto().getId());
        }
        return "user/user-info-upload";
    }

    @PostMapping("/users/{username}/upload")
    public String uploadImage(@PathVariable String username, @RequestParam("photo") MultipartFile file, Model m)
            throws IOException {
        Candidate c = mgmtUserService.getCandidateDetailsOfUser(username);
        Photo photo = new Photo();
        photo.setFilename(file.getOriginalFilename());
        photo.setMimeType(file.getContentType());
        photo.setBytes(file.getBytes());
        if (photo.getBytes().length >= 17) {
            c.setPhoto(photoService.save(photo));
        }
        Optional<MgmtUser> uOpt = mgmtUserService.getUserById(username);
        if (uOpt.isPresent()) {
            MgmtUser u = uOpt.get();
            u.setDetails(candidateService.saveCandidate(c));
            mgmtUserService.saveUser(u);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, MgmtUserService.USER_NOT_FOUND);
        }
        return "redirect:/admin/users/" + username + "/edit";
    }

    private void updateQuery(Query q, QueryAdminDto form) {
        ArrayList<Gremium> gremien = new ArrayList<>();
        for (String gId : form.getGremien()) {
            Optional<Gremium> gOpt = gremiumService.findGremiumByAbbr(gId);
            if (gOpt.isPresent()) {
                gremien.add(gOpt.get());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, GremiumService.GREMIUM_NOT_FOUND);
            }
        }
        q.setGremien(Set.of(gremien.toArray(new Gremium[0])));
        q.setText(form.getTxt());
        queryService.saveQuery(q);
    }

}
