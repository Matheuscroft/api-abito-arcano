package com.matheus.api_abito_arcano.services;

import com.matheus.api_abito_arcano.dtos.AreaDTO;
import com.matheus.api_abito_arcano.dtos.response.AreaResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.SubareaResponseDTO;
import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.repositories.AreaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AreaService {

    @Autowired
    private AreaRepository areaRepository;

    @PostConstruct
    public void garantirAreaSemCategoria() {
        Optional<Area> areaSemCategoria = areaRepository.findByNome("Sem Categoria");
        if (areaSemCategoria.isEmpty()) {
            Area area = new Area();
            area.setNome("Sem Categoria");
            area.setCor("#808080"); // Cor cinza padrão ou qualquer outra cor que você preferir
            areaRepository.save(area);
        }
    }

    public Area criarArea(AreaDTO areaDTO) {
        Area area = new Area();
        area.setNome(areaDTO.nome());
        area.setCor(areaDTO.cor());
        return areaRepository.save(area);
    }

    public List<AreaResponseDTO> listarAreas() {
        List<Area> areas = areaRepository.findAll();

        return areas.stream()
                .map(area -> new AreaResponseDTO(
                        area.getId(),
                        area.getNome(),
                        area.getCor(),
                        area.getSubareas().stream()
                                .map(subarea -> new SubareaResponseDTO(subarea.getId(), subarea.getNome()))
                                .toList()
                ))
                .collect(Collectors.toList());
    }

    public Optional<Area> buscarPorId(UUID id) {
        return areaRepository.findById(id);
    }

    public Area atualizarArea(UUID id, AreaDTO areaDTO) {
        Optional<Area> areaOptional = areaRepository.findById(id);
        if (areaOptional.isPresent()) {
            Area area = areaOptional.get();
            area.setNome(areaDTO.nome());
            area.setCor(areaDTO.cor());
            return areaRepository.save(area);
        }
        return null;
    }

    public boolean deletarArea(UUID id) {
        Optional<Area> areaOptional = areaRepository.findById(id);
        if (areaOptional.isPresent()) {
            areaRepository.delete(areaOptional.get());
            return true;
        }
        return false;
    }
}
