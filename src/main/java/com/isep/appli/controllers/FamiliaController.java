package com.isep.appli.controllers;

import com.isep.appli.dbModels.*;
import com.isep.appli.models.enums.ChatRoomType;
import com.isep.appli.models.enums.Race;
import com.isep.appli.services.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.isep.appli.controllers.UserController.checkIsUser;

@Controller
@RequestMapping("familia")
@RequiredArgsConstructor
public class FamiliaController {

    @PersistenceContext
    private EntityManager entityManager;
    private final ImageService imageService;
    private final FamiliaService familiaService;
    private final PersonnageService personnageService;
    private final PersonnageController personnageController;
    private final JoinRequestService joinRequestService;
    private final ChatRoomService chatRoomService;

    // Affiche la page d'une familia spécifique en fonction de son id
    // Si le personnage est le leader, affiche des paramètres en plus sur la page
    @GetMapping("/{familiaId}")
    public String familiaPage(@PathVariable Long familiaId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (UserController.checkIsUser(user, model).equals("200")){model.addAttribute("user", user);}

        Familia familia = entityManager.find(Familia.class, familiaId);
        if (familia == null) {
            throw new IllegalArgumentException("Cette familia n'existe pas.");
        }

        Personnage leader = familiaService.findLeader(familia);
        if (leader == null) {
            throw new IllegalArgumentException("Le nom du leader n'est pas correct.");
        }

        List<Personnage> members = personnageService.getPersonnagesByFamiliaId(familiaId);

        List<String> distinctRaces = new ArrayList<>();
        for (Race race : Race.values()) {
            distinctRaces.add(race.getDisplayName());
        }
        model.addAttribute("distinctRaces", distinctRaces);


        // Filtrer la liste des membres pour enlever le leader
        List<Personnage> filteredMembers = members.stream()
                .filter(member -> !member.getId().equals(leader.getId()))
                .collect(Collectors.toList());

        List<JoinRequest> pendingRequests = joinRequestService.getPendingRequestsByFamilia(familia);

        model.addAttribute("familia", familia);
        model.addAttribute("leader", leader);
        model.addAttribute("members", filteredMembers);
        model.addAttribute("familiaId", familia.getId());
        model.addAttribute("pendingRequests", pendingRequests);

        // Vérification si le personnage peut rejoindre une familia et s'il a une requête en cours
        Personnage personnage = personnageController.getSessionPersonnage(session).getBody();
        if (personnage != null && personnage.getFamilia() == null && personnage.getRace() != Race.GOD) {
            boolean hasPendingRequest = pendingRequests.stream()
                    .anyMatch(request -> request.getPersonnage().getId().equals(personnage.getId()));
            model.addAttribute("canJoin", !hasPendingRequest);
            model.addAttribute("hasPendingRequest", hasPendingRequest);
        } else {
            model.addAttribute("canJoin", false);
            model.addAttribute("hasPendingRequest", false);
        }

        boolean isLeader = personnage != null && personnage.getId().equals(leader.getId());
        model.addAttribute("isLeader", isLeader);

        return "familiaPage";
    }

    //Affiche la page de création d'une nouvelle familia
    @GetMapping("/new")
    public String createFamiliaPage(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        String checkUser = checkIsUser(user, model);
        if (!checkUser.equals("200")){return checkUser;}

        Personnage personnage = personnageController.getSessionPersonnage(session).getBody();
        if (personnage != null){
            model.addAttribute("familia", new Familia());
            return "newFamilia";
        } else {
            return "redirect:/home";
        }

    }

    //Création d'une nouvelle familia
    @PostMapping("/new")
    public String createFamilia(@Valid Familia familia, @RequestParam("croppedImageData") String croppedImageData, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        String checkUser = checkIsUser(user, model);
        if (!checkUser.equals("200")) { return checkUser; }

        Personnage personnage = personnageController.getSessionPersonnage(session).getBody();
        if (personnage != null && personnage.getFamilia() == null && personnage.getRace().equals(Race.GOD)) {
            byte[] decodedImageData = Base64.getDecoder().decode(croppedImageData);
            try {
                byte[] compressedImageData = imageService.compressImage(new ByteArrayInputStream(decodedImageData));
                if (familiaService.createFamilia(compressedImageData, personnage, familia)) {
                    ChatRoom familiaChatRoom = ChatRoom.builder()
                            .name(personnage.getFirstName() + '_' + personnage.getLastName())
                            .conversationType(ChatRoomType.GROUP)
                            .build();
                    model.addAttribute("newChatRoom", familiaChatRoom);
                    return "redirect:/familia/" + familia.getId();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "redirect:/home";
    }

    // Affiche la liste des familias existantes
    // Si le personnage est un dieu il a également accès a un bouton pour en créer une
    @GetMapping("/list")
    public String familiaList(Model model, HttpSession session){
        User user = (User) session.getAttribute("user");
        if (UserController.checkIsUser(user, model).equals("200")){model.addAttribute("user", user);}

        Map<Familia, Personnage> familiasWithLeaders = familiaService.getAllFamiliasWithLeaders();
        model.addAttribute("familiasWithLeaders", familiasWithLeaders);

        Personnage personnage = personnageController.getSessionPersonnage(session).getBody();
        if (personnage != null && personnage.getFamilia() == null && personnage.getRace()==Race.GOD) {
            model.addAttribute("canCreate", true);
        } else {
            model.addAttribute("canCreate", false);
        }

        return "familiaList";
    }

    // Redirige le personnage vers la page appropriée en fonction de son appartenance a une familia ou non
    @GetMapping("/redirect")
    public String redirectBasedOnFamilia(HttpSession session, Model model){
        User user = (User) session.getAttribute("user");
        String checkUser = checkIsUser(user, model);
        if (!checkUser.equals("200")) { return checkUser; }

        Personnage personnage = personnageController.getSessionPersonnage(session).getBody();
        if (personnage != null && personnage.getFamilia() != null) {
            return "redirect:/familia/" + personnage.getFamilia().getId();
        } else {
            return "redirect:/familia/list";
        }

    }

    // Créer une requête de demande de rejoindre une familia
    @PostMapping("/request/{familiaId}")
    public String requestJoinFamilia(@PathVariable Long familiaId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        String checkUser = checkIsUser(user, model);
        if (!checkUser.equals("200")) { return checkUser; }

        Familia familia = entityManager.find(Familia.class, familiaId);
        if (familia == null) {
            throw new IllegalArgumentException("Cette familia n'existe pas.");
        }

        Personnage personnage = personnageController.getSessionPersonnage(session).getBody();
        if (personnage != null && personnage.getFamilia() == null) {
            joinRequestService.createJoinRequest(personnage, familia);
            return "redirect:/familia/" + familiaId;
        }
        return "redirect:/home";
    }

    // Accepte une demande de rejoindre une familia
    @PostMapping("/requests/accept/{requestId}")
    public String acceptJoinRequest(@PathVariable Long requestId) {
        joinRequestService.acceptJoinRequest(requestId);
        JoinRequest joinRequest = joinRequestService.getJoinRequestById(requestId);
        return "redirect:/familia/" + joinRequest.getFamilia().getId();
    }

    // Rejette une demande de rejoindre une familia
    @PostMapping("/requests/reject/{requestId}")
    public String rejectJoinRequest(@PathVariable Long requestId) {
        JoinRequest joinRequest = joinRequestService.getJoinRequestById(requestId);
        joinRequestService.rejectJoinRequest(requestId);
        return "redirect:/familia/" + joinRequest.getFamilia().getId();
    }

    // Supprime un membre d'une familia
    @PostMapping("/removeMember/{familiaId}/{memberId}")
    public String removeMember(@PathVariable Long familiaId, @PathVariable Long memberId) {
        familiaService.removeMember(familiaId, memberId);
        return "redirect:/familia/" + familiaId;
    }

    // Supprime une familia
    @PostMapping("/delete/{familiaId}")
    public String deleteFamilia(@PathVariable Long familiaId) {
        familiaService.deleteFamiliaByIdWithMembers(familiaId);
        return "redirect:/familia/list";
    }
}