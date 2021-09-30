package de.astahsrm.gremiomat.gremium;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.server.ResponseStatusException;

import de.astahsrm.gremiomat.candidate.Candidate;
import de.astahsrm.gremiomat.candidate.answer.CandidateAnswer;
import de.astahsrm.gremiomat.query.Query;
import de.astahsrm.gremiomat.query.QueryFormSimple;
import de.astahsrm.gremiomat.query.QueryService;

@Controller
@RequestMapping("/")
@SessionAttributes("userAnswers")
public class GremiumController {

    private static final String USER_ANSWERS = "userAnswers";

    @Autowired
    private GremiumService gremiumService;

    @ModelAttribute(USER_ANSWERS)
    public void initSession(Model m) {
        if (m.getAttribute(USER_ANSWERS) == null) {
            m.addAttribute(USER_ANSWERS, new HashMap<Query, Integer>());
        }
    }

    @GetMapping
    public String getGremien(Model m) {
        List<Gremium> fsrGremien = new ArrayList<>();
        List<Gremium> fbrGremien = new ArrayList<>();
        List<Gremium> gremien = new ArrayList<>();
        for (Gremium gremium : gremiumService.getAllGremiumsSortedByName()) {
            if(gremium.getAbbr().contains("fbr")) {
                fbrGremien.add(gremium);
            }
            else if(gremium.getAbbr().contains("fsr")) {
                fsrGremien.add(gremium);
            }
            else {
                gremien.add(gremium);
            }
        }
        m.addAttribute("gremien", gremien);
        m.addAttribute("fsr", fsrGremien);
        m.addAttribute("fbr", fbrGremien);
        return "home";
    }

    @GetMapping("/{abbr}")
    public String getGremiumInfo(@PathVariable String abbr, Model m) {
        Optional<Gremium> gremiumOptional = gremiumService.getGremiumByAbbr(abbr);
        if (gremiumOptional.isPresent()) {
            m.addAttribute("gremium", gremiumOptional.get());
            return "gremien/info";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, GremiumService.GREMIUM_NOT_FOUND);
    }

    @GetMapping("/{abbr}/queries/{queryIndex}")
    public String getQuery(@SessionAttribute HashMap<Query, Integer> userAnswers, @PathVariable String abbr,
            @PathVariable int queryIndex, Model m) {
        Optional<Gremium> gremiumOptional = gremiumService.getGremiumByAbbr(abbr);
        if (gremiumOptional.isPresent()) {
            Gremium gremium = gremiumOptional.get();
            if (gremium.getContainedQueries().size() > queryIndex) {
                m.addAttribute("gremium", gremium);
                m.addAttribute("queryListSize", gremium.getContainedQueries().size());
                m.addAttribute("query", gremium.getContainedQueries().get(queryIndex));
                m.addAttribute("queryIndex", queryIndex);
                m.addAttribute("queryForm", new QueryFormSimple());
                m.addAttribute("isQueriesAnswered", userAnswers.size() == gremium.getContainedQueries().size()
                        || queryIndex == gremium.getContainedQueries().size() - 1);
                return "gremien/query";
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, QueryService.QUERY_NOT_FOUND);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, GremiumService.GREMIUM_NOT_FOUND);
    }

    @PostMapping(value = "/{abbr}/queries/{queryIndex}", params = "next-query")
    public String nextQueryPost(@SessionAttribute HashMap<Query, Integer> userAnswers,
            @ModelAttribute QueryFormSimple form, @PathVariable String abbr, @PathVariable int queryIndex, Model m) {
        Optional<Gremium> gremiumOptional = gremiumService.getGremiumByAbbr(abbr);
        if (gremiumOptional.isPresent()) {
            Gremium gremium = gremiumOptional.get();
            if (gremium.getContainedQueries().size() > queryIndex) {
                userAnswers.put(gremium.getContainedQueries().get(queryIndex), form.getOpinion());
                m.addAttribute(USER_ANSWERS, userAnswers);
                return "redirect:/" + abbr + "/queries/" + Integer.toString(queryIndex + 1);
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, QueryService.QUERY_NOT_FOUND);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, GremiumService.GREMIUM_NOT_FOUND);
    }

    @PostMapping(value = "/{abbr}/queries/{queryIndex}", params = "prev-query")
    public String prevQueryPost(@SessionAttribute HashMap<Query, Integer> userAnswers,
            @ModelAttribute QueryFormSimple form, @PathVariable String abbr, @PathVariable int queryIndex, Model m) {
        Optional<Gremium> gremiumOptional = gremiumService.getGremiumByAbbr(abbr);
        if (gremiumOptional.isPresent()) {
            Gremium gremium = gremiumOptional.get();
            if (gremium.getContainedQueries().size() > queryIndex) {
                userAnswers.put(gremium.getContainedQueries().get(queryIndex), form.getOpinion());
                m.addAttribute(USER_ANSWERS, userAnswers);
                return "redirect:/" + abbr + "/queries/" + Integer.toString(queryIndex - 1);
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, QueryService.QUERY_NOT_FOUND);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, GremiumService.GREMIUM_NOT_FOUND);
    }

    @PostMapping(value = "/{abbr}/queries/{queryIndex}", params = "results")
    public String resultsPost(@SessionAttribute HashMap<Query, Integer> userAnswers,
            @ModelAttribute QueryFormSimple form, @PathVariable String abbr, @PathVariable int queryIndex, Model m) {
        Optional<Gremium> gremiumOptional = gremiumService.getGremiumByAbbr(abbr);
        if (gremiumOptional.isPresent()) {
            Gremium gremium = gremiumOptional.get();
            if (gremium.getContainedQueries().size() > queryIndex) {
                userAnswers.put(gremium.getContainedQueries().get(queryIndex), form.getOpinion());
                m.addAttribute(USER_ANSWERS, userAnswers);
                return "redirect:/" + abbr + "/queries/results";
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, QueryService.QUERY_NOT_FOUND);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, GremiumService.GREMIUM_NOT_FOUND);
    }

    @GetMapping("/{abbr}/queries/results")
    public String getResults(@SessionAttribute HashMap<Query, Integer> userAnswers, @PathVariable String abbr,
            Model m) {
        Optional<Gremium> gremiumOptional = gremiumService.getGremiumByAbbr(abbr);
        if (gremiumOptional.isPresent()) {
            Gremium gremium = gremiumOptional.get();
            HashMap<Candidate, Double> compatibility = new HashMap<>();
            // Goes through every candidate in gremium
            for (Candidate candidate : gremium.getJoinedCandidates()) {
                double percentage = 0;
                double answersInCommon = 0;
                // Goes through every entry inside of 'userAnswers' Map
                for (Map.Entry<Query, Integer> entry : userAnswers.entrySet()) {
                    /*
                     * Finds matching Query from 'userAnswers' Map and compares it to candidate's
                     * answer. Increments 'answersInCommon' if candidate's answer and user's answer
                     * are equal.
                     */
                    for (CandidateAnswer ans : candidate.getAnswers()) {
                        if (ans.getQuery().equals(entry.getKey())) {
                            if (entry.getValue() == ans.getOpinion()) {
                                answersInCommon++;
                            }
                            break;
                        }
                    }
                }
                percentage = (answersInCommon / userAnswers.size()) * 100;
                compatibility.put(candidate, percentage);
            }
            m.addAttribute("comp", compatibility);
            return "gremien/results";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, GremiumService.GREMIUM_NOT_FOUND);
    }

    @PostMapping("/{abbr}/results")
    public String getResults(SessionStatus status) {
        status.setComplete();
        return "redirect:/";
    }

}
