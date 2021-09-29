package de.astahsrm.gremiomat.security;

import java.util.List;
import java.util.Optional;

import de.astahsrm.gremiomat.candidate.Candidate;

public interface MgmtUserService {
    String USER_NOT_FOUND = "No such User exists.";

    public String getRoleOfUserById(String uid);

    public Candidate getCandidateDetailsOfUser(String uid);

    public Optional<MgmtUser> getUserById(String name);

    public MgmtUser saveUser(MgmtUser u);

    public List<MgmtUser> getAllUsersSortedByUsername();

    public void delUser(MgmtUser username);

    public void delUserById(String username);

    public void lockAllUsers();
}
