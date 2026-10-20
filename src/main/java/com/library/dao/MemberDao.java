package com.library.dao;

import com.library.db.DatabaseManager;
import com.library.exception.LibraryException;
import com.library.exception.RecordNotFoundException;
import com.library.model.Member;
import com.library.model.MembershipType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Data access object for the {@code members} table. */
public class MemberDao extends AbstractDao<Member> {

    public MemberDao(DatabaseManager db) {
        super(db);
    }

    @Override
    public String entityName() {
        return "Member";
    }

    @Override
    protected String tableName() {
        return "members";
    }

    @Override
    protected Member mapRow(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("id"));
        member.setName(rs.getString("name"));
        member.setEmail(rs.getString("email"));
        member.setPhone(rs.getString("phone"));
        member.setMembershipType(MembershipType.valueOf(rs.getString("membership_type")));
        member.setJoinDate(LocalDate.parse(rs.getString("join_date")));
        return member;
    }

    @Override
    public Member create(Member member) throws LibraryException {
        int id = executeInsert("""
                INSERT INTO members (name, email, phone, membership_type, join_date)
                VALUES (?, ?, ?, ?, ?)""",
                member.getName(), member.getEmail(), member.getPhone(),
                member.getMembershipType().name(), member.getJoinDate().toString());
        member.setId(id);
        return member;
    }

    @Override
    public void update(Member member) throws LibraryException {
        int rows = executeUpdate("""
                UPDATE members SET name = ?, email = ?, phone = ?, membership_type = ?
                WHERE id = ?""",
                member.getName(), member.getEmail(), member.getPhone(),
                member.getMembershipType().name(), member.getId());
        if (rows == 0) {
            throw new RecordNotFoundException(entityName(), member.getId());
        }
    }

    public Optional<Member> findByEmail(String email) throws LibraryException {
        List<Member> result = query(selectSql() + " WHERE email = ? COLLATE NOCASE", email);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    public List<Member> searchByName(String keyword) throws LibraryException {
        return query(selectSql() + " WHERE lower(name) LIKE ? ORDER BY name", "%" + keyword.toLowerCase() + "%");
    }
}
