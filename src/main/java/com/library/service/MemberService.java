package com.library.service;

import com.library.dao.LoanDao;
import com.library.dao.MemberDao;
import com.library.db.DatabaseManager;
import com.library.exception.DuplicateRecordException;
import com.library.exception.LibraryException;
import com.library.exception.ValidationException;
import com.library.model.Member;

import java.util.List;
import java.util.Optional;

/** Business rules for managing library members. */
public class MemberService {

    private final DatabaseManager db;
    private final MemberDao memberDao;
    private final LoanDao loanDao;

    public MemberService(DatabaseManager db, MemberDao memberDao, LoanDao loanDao) {
        this.db = db;
        this.memberDao = memberDao;
        this.loanDao = loanDao;
    }

    public Member registerMember(Member member) throws LibraryException {
        member.validate();
        ensureEmailIsFree(member);
        return memberDao.create(member);
    }

    /** Fails early if the e-mail address already belongs to another member. */
    public void ensureEmailIsNew(String email) throws LibraryException {
        Member probe = new Member();
        probe.setEmail(email);
        ensureEmailIsFree(probe);
    }

    public List<Member> listMembers() throws LibraryException {
        return memberDao.findAll();
    }

    public Member getMember(int id) throws LibraryException {
        return memberDao.getById(id);
    }

    public List<Member> searchMembers(String keyword) throws LibraryException {
        if (keyword == null || keyword.isBlank()) {
            throw new ValidationException("Search keyword cannot be empty.");
        }
        return memberDao.searchByName(keyword.trim());
    }

    public void updateMember(Member member) throws LibraryException {
        member.validate();
        int active = loanDao.countActiveByMember(member.getId());
        if (active > member.getMembershipType().getBorrowLimit()) {
            throw new ValidationException("The member currently has " + active + " books on loan, which exceeds the "
                    + member.getMembershipType() + " limit of " + member.getMembershipType().getBorrowLimit() + ".");
        }
        ensureEmailIsFree(member);
        memberDao.update(member);
    }

    /** Deletes a member together with their (finished) loan history. */
    public void deleteMember(int id) throws LibraryException {
        Member member = memberDao.getById(id);
        if (loanDao.countActiveByMember(id) > 0) {
            throw new ValidationException(member.getName() + " must return all borrowed books before being removed.");
        }
        db.inTransaction(() -> {
            loanDao.deleteByMember(id);
            memberDao.delete(id);
            return null;
        });
    }

    private void ensureEmailIsFree(Member member) throws LibraryException {
        Optional<Member> existing = memberDao.findByEmail(member.getEmail());
        if (existing.isPresent() && existing.get().getId() != member.getId()) {
            throw new DuplicateRecordException("The e-mail " + member.getEmail() + " is already registered to "
                    + existing.get().getName() + ".");
        }
    }
}
