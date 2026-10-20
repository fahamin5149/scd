package com.library.model;

import com.library.exception.ValidationException;

import java.time.LocalDate;

/** A registered library member who is allowed to borrow books. */
public class Member extends Person {

    private MembershipType membershipType;
    private LocalDate joinDate;

    public Member() {
    }

    public Member(String name, String email, String phone, MembershipType membershipType) {
        super(name, email, phone);
        this.membershipType = membershipType;
        this.joinDate = LocalDate.now();
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public void setMembershipType(MembershipType membershipType) {
        this.membershipType = membershipType;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    public String getRole() {
        return membershipType + " member";
    }

    @Override
    public void validate() throws ValidationException {
        super.validate();
        if (membershipType == null) {
            throw new ValidationException("Membership type is required.");
        }
    }

    @Override
    public String toString() {
        return "Member #" + getId() + " - " + getName() + " (" + getRole() + ")";
    }
}
