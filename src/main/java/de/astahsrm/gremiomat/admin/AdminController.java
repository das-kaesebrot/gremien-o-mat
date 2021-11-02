package de.astahsrm.gremiomat.admin;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
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
import de.astahsrm.gremiomat.mgmt.MgmtUserService;
import de.astahsrm.gremiomat.photo.PhotoService;
import de.astahsrm.gremiomat.query.Query;
import de.astahsrm.gremiomat.query.QueryAdminDto;
import de.astahsrm.gremiomat.query.QueryService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final String REDIRECT_ADMIN_QUERIES = "redirect:/admin/queries";

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
    public String newQuery(Model m, @PathVariable String abbr) {
        m.addAttribute("allGremien", gremiumService.getAllGremiumsSortedByName());
        m.addAttribute("form", new QueryAdminDto());
        m.addAttribute("abbr", abbr);
        return "admin/query";
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

    @PostMapping("/gremien/{abbr}/queries/new")
    public String saveNewQuery(@PathVariable String abbr) {
        return "forward:/admin/queries/new?abbr=" + abbr;
    }

    @PostMapping(value = "/gremien/{abbr}/queries/{id}")
    public String postSaveGremiumQueryEditPage(@PathVariable String abbr, @PathVariable long id) {
        return "forward:/admin/queries/" + id + "?abbr=" + abbr;
    }

    @GetMapping("/gremien/{abbr}/queries/{id}/delete")
    public String delGremiumQuery(@PathVariable String abbr, @PathVariable long id) {
        return "forward:/admin/queries/" + id + "/del?abbr=" + abbr;
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

    @GetMapping("/queries")
    public String getQueries(Model m) {
        m.addAttribute("allQueries", queryService.getAllQueries());
        m.addAttribute("allGremien", gremiumService.getAllGremiumsSortedByName());
        return "admin/queries";
    }

    @GetMapping("/queries/new")
    public String getNewQuery(Model m) {
        m.addAttribute("allGremien", gremiumService.getAllGremiumsSortedByName());
        m.addAttribute("form", new QueryAdminDto());
        return "admin/query";
    }

    @GetMapping("/queries/{id}")
    public String getQuery(Model m, @PathVariable long id) {
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
            return "admin/query";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, QueryService.QUERY_NOT_FOUND);
    }

    @PostMapping("/queries/new")
    public String newQuery(Model m, QueryAdminDto form, BindingResult res,
            @RequestParam(required = false) String abbr) {
        if (res.hasErrors()) {
            m.addAttribute("error", res.getAllErrors());
            return "admin/query";
        }
        Query q = new Query();
        if (abbr != null && !abbr.isBlank()) {
            Optional<Gremium> gOpt = gremiumService.findGremiumByAbbr(abbr);
            if (gOpt.isPresent()) {
                q.addGremium(gOpt.get());
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, GremiumService.GREMIUM_NOT_FOUND);
            }
        } else {
            q.setGremien(new HashSet<>());
        }
        q.setText(form.getTxt());
        queryService.saveQuery(q);
        if (abbr != null && !abbr.isBlank()) {
            return "redirect:/admin/gremien/" + abbr;
        } else {
            return REDIRECT_ADMIN_QUERIES;
        }
    }

    @PostMapping("/queries/{id}")
    public String updateQuery(Model m, QueryAdminDto form, BindingResult res, @PathVariable long id,
            @RequestParam(required = false) String abbr) {
        if (res.hasErrors()) {
            m.addAttribute("error", res.getAllErrors());
            return "admin/query";
        }
        Optional<Query> qOpt = queryService.getQueryById(id);
        if (qOpt.isPresent()) {
            Query q = qOpt.get();
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
            if (abbr != null && !abbr.isBlank()) {
                return "redirect:/admin/gremien/" + abbr;
            } else {
                return REDIRECT_ADMIN_QUERIES;
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, QueryService.QUERY_NOT_FOUND);
        }
    }

    @GetMapping("/queries/{id}/del")
    public String deleteQuery(Model m, @RequestParam(required = false) String abbr, @PathVariable long id) {
        queryService.delQueryById(id);
        if (abbr != null && !abbr.isBlank()) {
            return "redirect:/admin/gremien/" + abbr;
        } else {
            return REDIRECT_ADMIN_QUERIES;
        }
    }

    @PostMapping("/queries/csv")
    public String processQueryCSV(@RequestParam MultipartFile file,
            @RequestParam(required = false) String abbr) throws IOException, CsvException {
        queryService.saveQueriesFromCSV(file, abbr);
        return REDIRECT_ADMIN_QUERIES;
    }

}
