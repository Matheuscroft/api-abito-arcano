package com.matheus.api_abito_arcano.services;

import com.matheus.api_abito_arcano.dtos.AreaDTO;
import com.matheus.api_abito_arcano.dtos.response.AreaResponseDTO;
import com.matheus.api_abito_arcano.dtos.response.SubareaSimpleResponseDTO;
import com.matheus.api_abito_arcano.exceptions.AreaNotFoundException;
import com.matheus.api_abito_arcano.mappers.AreaMapper;
import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.User;
import com.matheus.api_abito_arcano.repositories.AreaRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AreaService {

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private UserService userService;

    public Area criarArea(AreaDTO areaDTO) {
        User user = userService.getUsuarioAutenticado();

        Area area = new Area();
        area.setName(areaDTO.name());
        area.setColor(areaDTO.color());
        area.setUser(user);

        return areaRepository.save(area);
    }


    public List<AreaResponseDTO> listarAreas() {
        User user = userService.getUsuarioAutenticado();
        List<Area> areas = areaRepository.findByUserId(user.getId());

        return areas.stream()
                .map(AreaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<Area> buscarPorId(UUID id) {
        User user = userService.getUsuarioAutenticado();
        return areaRepository.findById(id)
                .filter(area -> area.getUser().getId().equals(user.getId()));
    }


    public Area atualizarArea(UUID id, AreaDTO areaDTO) {
        User user = userService.getUsuarioAutenticado();
        Optional<Area> areaOptional = areaRepository.findById(id);

        if (areaOptional.isPresent()) {
            Area area = areaOptional.get();

            if (!area.getUser().getId().equals(user.getId())) {
                return null;
            }

            if (area.getName().equalsIgnoreCase("Sem Categoria")) {
                return null;
            }

            area.setName(areaDTO.name());
            area.setColor(areaDTO.color());
            return areaRepository.save(area);
        }

        return null;
    }


    public boolean deletarArea(UUID id) {
        User user = userService.getUsuarioAutenticado();
        Optional<Area> areaOptional = areaRepository.findById(id);

        if (areaOptional.isPresent()) {
            Area area = areaOptional.get();

            if (!area.getUser().getId().equals(user.getId())) {
                return false;
            }

            if (area.getName().equalsIgnoreCase("Sem Categoria")) {
                return false;
            }
            areaRepository.delete(area);
            return true;
        }
        return false;
    }

    public Area getAreaOrThrow(UUID areaId) {
        return areaRepository.findById(areaId)
                .orElseThrow(() -> new AreaNotFoundException(areaId));
    }

    public Area getValidOrDefaultArea(UUID areaId, UUID userId) {
        if (areaId != null) {
            Optional<Area> optionalArea = areaRepository.findByIdAndUserId(areaId, userId);
            if (optionalArea.isPresent()) {
                return optionalArea.get();
            }
        }

        return areaRepository.findByNameAndUserId("Sem Categoria", userId)
                .orElseThrow(() -> new AreaNotFoundException(UUID.randomUUID()));
    }


}
