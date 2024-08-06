package com.isep.appli.services;
import com.isep.appli.dbModels.Familia;
import com.isep.appli.dbModels.JoinRequest;
import com.isep.appli.dbModels.Personnage;

import com.isep.appli.models.enums.Race;
import com.isep.appli.repositories.FamiliaRepository;
import com.isep.appli.repositories.JoinRequestRepository;
import com.isep.appli.repositories.PersonnageRepository;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FamiliaService {
    private final FamiliaRepository familiaRepository;

    private final PersonnageRepository personnageRepository;
    private final JoinRequestRepository joinRequestRepository;


    FamiliaService(FamiliaRepository familiaRepository, PersonnageRepository personnageRepository, JoinRequestRepository joinRequestRepository) {
        this.familiaRepository = familiaRepository;
        this.personnageRepository = personnageRepository;
        this.joinRequestRepository = joinRequestRepository;
    }

    public Personnage findLeader(Familia familia){

        for(Personnage personnage : familia.getPersonnages()){
            if(personnage.getRace().equals(Race.GOD)){
                return personnage;
            }
        }
        return null;
    }

    // Récupère toutes les familias
    public Iterable<Familia> getAllFamilias() {
        return familiaRepository.findAll();
    }

    // Crée une nouvelle familia
    public boolean createFamilia(byte[] compressedImage, Personnage personnage, Familia familia) {
        String fullName = personnage.getFirstName() + ' ' + personnage.getLastName();
        familia.setName(fullName);
        familia.setEmbleme_image(Base64.getEncoder().encodeToString(compressedImage));
        familiaRepository.save(familia);

        personnage.setFamilia(familia);
        personnageRepository.save(personnage);
        return true;
    }

    // Récupère toutes les familias avec leurs leaders
    public Map<Familia, Personnage> getAllFamiliasWithLeaders() {
        Iterable<Familia> familias = getAllFamilias();
        Map<Familia, Personnage> familiaWithLeaders = new HashMap<>();
        for (Familia familia : familias) {
            Personnage leader = findLeader(familia);
            familiaWithLeaders.put(familia, leader);
        }
        return familiaWithLeaders;
    }

    // Ajoute un personnage à une familia
    public void joinFamilia(Personnage personnage, Familia familia){
        personnage.setFamilia(familia);
        personnageRepository.save(personnage);
    }

    // Supprime un personnage d'une familia
    public void removeMember(Long familiaId, Long memberId) {
        Familia familia = familiaRepository.findById(familiaId).orElse(null);
        if (familia != null) {
            List<Personnage> members = personnageRepository.findByFamiliaId(familiaId);
            Personnage memberToRemove = members.stream()
                    .filter(member -> member.getId().equals(memberId))
                    .findFirst()
                    .orElse(null);
            if (memberToRemove != null) {
                memberToRemove.setFamilia(null); // Mettre à null le ID de famille du membre
                personnageRepository.save(memberToRemove); // Enregistrer les modifications du personnage
            } else {
                throw new IllegalArgumentException("Le personnage spécifié n'est pas membre de cette famille.");
            }
        } else {
            throw new IllegalArgumentException("La famille spécifiée n'existe pas.");
        }
    }

    // Supprime une familia et met à jour les personnages
    public void deleteFamiliaByIdWithMembers(Long familiaId) {
        Familia familia = familiaRepository.findById(familiaId).orElse(null);
        if (familia != null) {
            List<Personnage> members = personnageRepository.findByFamiliaId(familiaId);
            // Mettre tous les familiaId des membres à null
            for (Personnage member : members) {
                member.setFamilia(null);
                personnageRepository.save(member);
            }
            List<JoinRequest> joinRequests = joinRequestRepository.findByFamilia(familia);
            joinRequestRepository.deleteAll(joinRequests);
            familiaRepository.deleteById(familiaId);
        }
    }
}