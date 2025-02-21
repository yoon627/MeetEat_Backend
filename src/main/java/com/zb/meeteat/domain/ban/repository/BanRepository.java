package com.zb.meeteat.domain.ban.repository;

import com.zb.meeteat.domain.ban.entity.Ban;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BanRepository extends JpaRepository<Ban, Long> {

  void deleteByBannerIdAndBannedId(Long bannerId, Long bannedId);

  Ban findByBannerIdAndBannedId(Long bannerId, Long bannedId);
}
