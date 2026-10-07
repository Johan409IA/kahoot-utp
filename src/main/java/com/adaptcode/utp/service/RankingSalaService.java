package com.adaptcode.utp.service;

import java.util.List;

import com.adaptcode.utp.dto.response.RankingSalaResponse;

public interface RankingSalaService {

	List<RankingSalaResponse> mostrarRanking(Long salaId);
}
