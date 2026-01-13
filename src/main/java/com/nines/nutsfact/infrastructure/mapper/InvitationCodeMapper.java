package com.nines.nutsfact.infrastructure.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nines.nutsfact.domain.model.user.InvitationCode;

@Mapper
public interface InvitationCodeMapper {
    List<InvitationCode> findAll();
    List<InvitationCode> findByBusinessAccountId(@Param("businessAccountId") Integer businessAccountId);
    InvitationCode findById(@Param("id") Integer id);
    InvitationCode findByCode(@Param("code") String code);
    InvitationCode findByCodeAndEmail(@Param("code") String code, @Param("email") String email);
    void insert(InvitationCode invitationCode);
    void update(InvitationCode invitationCode);
    void markAsUsed(@Param("id") Integer id);
    void delete(@Param("id") Integer id);
}
