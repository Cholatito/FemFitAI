package pe.edu.upc.femfitai.services.implementations;

import pe.edu.upc.femfitai.services.interfaces.ICiclosService;

import pe.edu.upc.femfitai.dtos.CiclosUsuarioDTO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import pe.edu.upc.femfitai.dtos.CiclosDTO;
import pe.edu.upc.femfitai.dtos.CiclosRequestDTO;
import pe.edu.upc.femfitai.entities.Ciclos;
import pe.edu.upc.femfitai.entities.Usuarios;
import pe.edu.upc.femfitai.repositories.CiclosRepository;
import pe.edu.upc.femfitai.repositories.UsuariosRepository;

@Service
public class CiclosService implements ICiclosService {

    private final CiclosRepository repository;
    private final UsuariosRepository usuariosRepository;
