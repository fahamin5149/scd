package com.library.ui;

import com.library.exception.LibraryException;
import com.library.model.Member;
import com.library.model.MembershipType;
import com.library.service.MemberService;
import com.library.util.TablePrinter;
import com.library.util.Validator;

import java.util.List;
import java.util.function.Predicate;

/** Console screens for creating, reading, updating and deleting members. */
public class MemberMenu extends Menu {

    private static final String EMAIL_RULE = "Invalid e-mail address (example: name@example.com).";
    private static final String PHONE_RULE = "Phone number must contain 7-15 digits, optionally starting with '+'.";

    private final MemberService service;

    public MemberMenu(InputReader in, MemberService service) {
        super(in, "MEMBER MANAGEMENT", "Back to main menu");
        this.service = service;
        option("Register a new member", this::addMember);
        option("View all members", this::listMembers);
        option("Search members by name", this::searchMembers);
        option("View member details", this::viewMember);
        option("Update a member", this::updateMember);
        option("Delete a member", this::deleteMember);
    }

    private void addMember() throws LibraryException {
        String name = in.readRequired("Full name : ");
        String email = in.readMatching("E-mail    : ", Validator::isValidEmail, EMAIL_RULE);
        service.ensureEmailIsNew(email);
        String phone = in.readMatching("Phone     : ", Validator::isValidPhone, PHONE_RULE);
        System.out.println("Membership type:");
        MembershipType type = in.readEnum("Choose", MembershipType.class, null);

        Member member = service.registerMember(new Member(name, email, phone, type));
        ConsoleUtil.success("Member registered successfully with ID " + member.getId() + ".");
    }

    private void listMembers() throws LibraryException {
        printMembers(service.listMembers());
    }

    private void searchMembers() throws LibraryException {
        printMembers(service.searchMembers(in.readRequired("Enter part of the name: ")));
    }

    private void viewMember() throws LibraryException {
        Member m = service.getMember(in.readId("Enter member ID: "));
        System.out.println();
        System.out.println("  ID           : " + m.getId());
        System.out.println("  Name         : " + m.getName());
        System.out.println("  E-mail       : " + m.getEmail());
        System.out.println("  Phone        : " + m.getPhone());
        System.out.println("  Membership   : " + m.getMembershipType());
        System.out.println("  Borrow limit : " + m.getMembershipType().getBorrowLimit() + " books");
        System.out.println("  Loan period  : " + m.getMembershipType().getLoanDays() + " days");
        System.out.println("  Joined on    : " + m.getJoinDate());
    }

    private void updateMember() throws LibraryException {
        Member m = service.getMember(in.readId("Enter ID of the member to update: "));
        System.out.println("Editing " + m + ". Press Enter to keep the current value.");

        m.setName(in.readOptional("Name", m.getName()));
        m.setEmail(readValidOptional("E-mail", m.getEmail(), Validator::isValidEmail, EMAIL_RULE));
        m.setPhone(readValidOptional("Phone", m.getPhone(), Validator::isValidPhone, PHONE_RULE));
        System.out.println("Membership type:");
        m.setMembershipType(in.readEnum("Choose", MembershipType.class, m.getMembershipType()));

        service.updateMember(m);
        ConsoleUtil.success("Member #" + m.getId() + " updated successfully.");
    }

    private void deleteMember() throws LibraryException {
        Member m = service.getMember(in.readId("Enter ID of the member to delete: "));
        if (in.confirm("Delete " + m + "?")) {
            service.deleteMember(m.getId());
            ConsoleUtil.success("Member deleted.");
        } else {
            ConsoleUtil.info("Deletion cancelled.");
        }
    }

    private String readValidOptional(String label, String current, Predicate<String> rule, String message) {
        while (true) {
            String value = in.readOptional(label, current);
            if (rule.test(value)) {
                return value;
            }
            System.out.println("  [!] " + message);
        }
    }

    static void printMembers(List<Member> members) {
        if (members.isEmpty()) {
            ConsoleUtil.info("No members found.");
            return;
        }
        TablePrinter table = new TablePrinter("ID", "Name", "E-mail", "Phone", "Type", "Joined");
        for (Member m : members) {
            table.addRow(m.getId(), m.getName(), m.getEmail(), m.getPhone(), m.getMembershipType(), m.getJoinDate());
        }
        table.print();
    }
}
