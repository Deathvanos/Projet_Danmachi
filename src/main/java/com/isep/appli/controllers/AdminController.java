package com.isep.appli.controllers;

import com.isep.appli.dbModels.*;
import com.isep.appli.models.FormattedReport;
import com.isep.appli.models.enums.ItemCategory;
import com.isep.appli.models.enums.Race;
import com.isep.appli.services.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Controller
@AllArgsConstructor
public class AdminController {

    private final UserService userService;
    private final PersonnageService personnageService;
    private final ReportService reportService;
    private final MessageService messageService;
    private final FamiliaService familiaService;
    private final ItemService itemService;
    private final ImageService imageService;
    private final InventoryService inventoryService;
    private final ShopService shopService;


    static public String checkIsAdmin(User userAdmin, Model model) {
        if (userAdmin==null) {return "errors/error-401";}
        if (!userAdmin.getIsAdmin()) {return "errors/error-403";}
        model.addAttribute("user", userAdmin);
        return "200";
    }

    @GetMapping({"/admin/home", "/admin/", "/admin"})
    public String adminHomePage(Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        // Import the html file
        return "admin/mainAdmin";
    }



    @GetMapping("/admin/users-management")
    public String usersManagementPage(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        // limit the maw size
        if (size>50) {return String.format("redirect:/admin/users-management?page=%d&size=50", page);}
        // Recover UserList
        Page<User> userPage = userService.getAllUsers(PageRequest.of(page, size));
        model.addAttribute("usersPages", userPage);

        // Add information for pagination
        int totalPages = userPage.getTotalPages();
        if (page > totalPages) {return String.format("redirect:/admin/users-management?page=%d&size=%d", userPage.getTotalPages() - 1, size);}
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        model.addAttribute("usersPersonnages", personnageService.getPersonnagesUsers(userPage.stream().toList()));
        return "admin/usersManagement";
    }


    @GetMapping("/admin/users-management/{id}")
    public String userDesciptionPage(@PathVariable long id, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        // find the user id
        User user = userService.getUserById(id);
        if (user == null) {return "errors/error-404";}
        model.addAttribute("userInfo", user);

        List<User> userList = new ArrayList<>();
        userList.add(userService.getUserById(id));
        model.addAttribute("userPersonnages", personnageService.getPersonnagesUsers(userList));

        return "admin/userDescription";
    }

    @PostMapping("/admin/deleteUser/{id}")
    public String deleteUser(@PathVariable long id, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        List<User> userList = new ArrayList<>();
        userList.add(userService.getUserById(id));
        for (Personnage p: personnageService.getPersonnagesUsers(userList)) {
            System.out.println(p);
            deletePlayer(p.getId());
        }
        // destroy user
        userService.deleteUser(id);
        return "redirect:/admin/users-management";
    }

    @PostMapping("/admin/updateUser/{id}")
    public String updateUser(@PathVariable long id, @Valid User user,Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        userService.updateUser(id, user);
        return "redirect:/admin/users-management/"+id;
    }


    @GetMapping("/admin/players-management/{id}")
    public String infoPlayerPage(@PathVariable long id, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        // find the personnage id
        Personnage personnage = personnageService.getPersonnageById(id);
        if (personnage == null) {return "errors/error-404";}
        model.addAttribute("playerInfo", personnage);
        model.addAttribute("familia", new Familia());
        model.addAttribute("raceCategory", Race.values());
        model.addAttribute("familiaList", familiaService.getAllFamilias());
        model.addAttribute("inventory", inventoryService.getPlayerInventory(personnage));
        model.addAttribute("itemsList", itemService.getAll());

        return "admin/playerDescription";
    }

    private void deletePlayer(long idPlayer) {
        Personnage player = personnageService.getPersonnageById(idPlayer);
        // remove from shop
        for (Shop shop: shopService.getPlayerShop(player)) {
            shopService.delete(shop);
        }
        // remove from inventory
        for (Inventory inventory: inventoryService.getPlayerInventory(player)) {
            inventoryService.delete(inventory);
        }
        // remove familia players user
        Familia familia = player.getFamilia();
        if (familia != null &&  familiaService.findLeader(familia).getId() == idPlayer) {
            familiaService.deleteFamiliaByIdWithMembers(familia.getId());
        }
        // destroy players user
        personnageService.deletePersonnageById(idPlayer);
    }

    @PostMapping("/admin/deletePlayer/{idUser}/{idPlayer}")
    public String deletePlayerPage(@PathVariable long idUser, @PathVariable long idPlayer, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        deletePlayer(idPlayer);
        return "redirect:/admin/users-management/"+idUser;
    }

    @PostMapping("/admin/updatePlayer/{idUser}/{idPlayer}")
    public String updatePlayer(@PathVariable long idUser, @PathVariable long idPlayer, @Valid Personnage player,Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        personnageService.updatePlayer(idPlayer, player);
        return "redirect:/admin/players-management/"+idPlayer;
    }

    @PostMapping("admin/delete/{idPlayer}/{familiaId}")
    public String deleteFamilia(@PathVariable Long idPlayer, @PathVariable Long familiaId, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        familiaService.deleteFamiliaByIdWithMembers(familiaId);
        return "redirect:/admin/players-management/"+idPlayer;
    }




    @PostMapping("admin/addItemUser/{idUser}/{idPlayer}")
    public String addItemUser(@PathVariable Long idUser, @PathVariable Long idPlayer, @RequestParam("idItem") Long idItem, @RequestParam("nbItem") int nbItem, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        inventoryService.addItemForPlayer(personnageService.getPersonnageById(idPlayer), itemService.findById(idItem), nbItem);

        return "redirect:/admin/players-management/"+idPlayer;
    }

    @PostMapping("admin/deleteItemUser/{idPlayer}/{idItem}")
    public String deleteItemUser(@PathVariable Long idPlayer, @PathVariable Long idItem,  Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        Inventory inventory = inventoryService.findByItemId(personnageService.getPersonnageById(idPlayer), itemService.findById(idItem));
        inventoryService.removeItemInInventory(inventory, inventory.getQuantity());

        return "redirect:/admin/players-management/"+idPlayer;
    }


    @GetMapping("/admin/items-management")
    public String itemsManagementPage(Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        model.addAttribute("itemsList", itemService.findAllItems());
        model.addAttribute("ItemCategory", ItemCategory.values());
        model.addAttribute("newItem", new Item());
        model.addAttribute("updateItem", new Item());

        return "admin/itemsManagement";
    }

    @PostMapping("/admin/newItem")
    public String newItem(@Valid Item newItem, @RequestParam("croppedImageData") String croppedImageData, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        byte[] decodedImageData = Base64.getDecoder().decode(croppedImageData);
        itemService.save(newItem, new ByteArrayInputStream(decodedImageData));

        return "redirect:/admin/items-management";
    }

    @PostMapping("/admin/updateItem/{id}")
    public String updateItem(@PathVariable long id,@Valid Item updateItem, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        itemService.updateItem(id, updateItem);

        return "redirect:/admin/items-management";
    }


    @PostMapping("/admin/deleteItem/{id}")
    public String deleteItem(@PathVariable long id, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        itemService.delete(id);

        return "redirect:/admin/items-management";
    }


    @GetMapping("/admin/shop-management")
    public String shopManagementPage(Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        model.addAttribute("shopList", shopService.getAll());
        model.addAttribute("itemsList", itemService.findAllItems());
        model.addAttribute("playerList", personnageService.getAllPersonnages());
        Shop newShop = new Shop();
        newShop.setPrice(1000L);
        newShop.setQuantity(1);
        model.addAttribute("newShop", newShop);


        return "admin/shopManagement";
    }

    @PostMapping("/admin/deleteShop/{id}")
    public String deleteShop(@PathVariable long id, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        shopService.delete(shopService.findById(id));

        return "redirect:/admin/shop-management";
    }

    @PostMapping("/admin/newShop")
    public String newShop(@Valid Shop newShop, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}

        shopService.save(newShop);

        return "redirect:/admin/shop-management";
    }


    /* ************************************************************************************************************** */
    /* *********************************************** LISA PART **************************************************** */
    /* ************************************************************************************************************** */

    @GetMapping("/admin/reportList")
    public String reportListPageEmpty(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return "redirect:/admin/reportList/0?page=0&size=10";
    }

    @GetMapping("/admin/reportList/{reportId}")
    public String reportListPage(@PathVariable Long reportId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, Model model, HttpSession session) {
        // Check admin connexion
        User userAdmin = (User) session.getAttribute("user");
        String checkUser = checkIsAdmin(userAdmin, model);
        if (!checkUser.equals("200")){return checkUser;}
        // limit the maw size
        if (size>50) {return String.format("redirect:/admin/reportList/0?page=%d&size=50", page);}

        Page<FormattedReport> reportPage = reportService.getAllFormattedReportSortedByDate(PageRequest.of(page, size, Sort.by("date").descending()));
        model.addAttribute("reportPage", reportPage);

        Report reportSelected = reportService.getById(reportId);

        if (reportSelected != null) {
            model.addAttribute("reportSelected", reportService.convertToFormattedReport(reportSelected));
            if (reportSelected.getObjectReportedType().equals("MESSAGE")) {
                model.addAttribute("messageReported", messageService.findById(reportSelected.getObjectReportedId()));
            }
        }

        // Add information for pagination
        int totalPages = reportPage.getTotalPages();
        if (page > totalPages) {return String.format("redirect:/admin/reportList?page=%d&size=%d", reportPage.getTotalPages() - 1, size);}
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "admin/adminReportPage";
    }



}


/*
INSERT INTO USER values();
    id, email, enable, first_name, is_admin, last_name, password, username
INSERT INTO USER values(10, 'fictif10@yahoo.fr', 1, 'testFirst10', 0, 'testLast10', 'root', 'fictif10');
INSERT INTO USER values(11, 'fictif11@yahoo.fr', 1, 'testFirst11', 0, 'testLast11', 'root', 'fictif11');
INSERT INTO USER values(12, 'fictif12@yahoo.fr', 1, 'testFirst12', 0, 'testLast12', 'root', 'fictif12');
INSERT INTO USER values(13, 'fictif13@yahoo.fr', 1, 'testFirst13', 0, 'testLast13', 'root', 'fictif13');
INSERT INTO USER values(14, 'fictif14@yahoo.fr', 1, 'testFirst14', 0, 'testLast14', 'root', 'fictif14');
INSERT INTO USER values(15, 'fictif15@yahoo.fr', 1, 'testFirst15', 0, 'testLast15', 'root', 'fictif15');
INSERT INTO USER values(16, 'fictif16@yahoo.fr', 1, 'testFirst16', 0, 'testLast16', 'root', 'fictif16');
INSERT INTO USER values(17, 'fictif17@yahoo.fr', 1, 'testFirst17', 0, 'testLast17', 'root', 'fictif17');
INSERT INTO USER values(18, 'fictif18@yahoo.fr', 1, 'testFirst18', 0, 'testLast18', 'root', 'fictif18');
INSERT INTO USER values(19, 'fictif19@yahoo.fr', 1, 'testFirst19', 0, 'testLast19', 'root', 'fictif19');
INSERT INTO USER values(20, 'fictif20@yahoo.fr', 1, 'testFirst20', 0, 'testLast20', 'root', 'fictif20');
INSERT INTO USER values(21, 'fictif21@yahoo.fr', 1, 'testFirst21', 0, 'testLast21', 'root', 'fictif21');
INSERT INTO USER values(22, 'fictif22@yahoo.fr', 1, 'testFirst22', 0, 'testLast22', 'root', 'fictif22');
INSERT INTO USER values(23, 'fictif23@yahoo.fr', 1, 'testFirst23', 0, 'testLast23', 'root', 'fictif23');
INSERT INTO USER values(24, 'fictif24@yahoo.fr', 1, 'testFirst24', 0, 'testLast24', 'root', 'fictif24');
INSERT INTO USER values(25, 'fictif25@yahoo.fr', 1, 'testFirst25', 0, 'testLast25', 'root', 'fictif25');
INSERT INTO USER values(26, 'fictif26@yahoo.fr', 1, 'testFirst26', 0, 'testLast26', 'root', 'fictif26');
INSERT INTO USER values(27, 'fictif27@yahoo.fr', 1, 'testFirst27', 0, 'testLast27', 'root', 'fictif27');
INSERT INTO USER values(28, 'fictif28@yahoo.fr', 1, 'testFirst28', 0, 'testLast28', 'root', 'fictif28');
INSERT INTO USER values(29, 'fictif29@yahoo.fr', 1, 'testFirst29', 0, 'testLast29', 'root', 'fictif29');
INSERT INTO USER values(30, 'fictif30@yahoo.fr', 1, 'testFirst30', 0, 'testLast30', 'root', 'fictif30');
INSERT INTO USER values(31, 'fictif31@yahoo.fr', 1, 'testFirst31', 0, 'testLast31', 'root', 'fictif31');
INSERT INTO USER values(32, 'fictif32@yahoo.fr', 1, 'testFirst32', 0, 'testLast32', 'root', 'fictif32');
INSERT INTO USER values(33, 'fictif33@yahoo.fr', 1, 'testFirst33', 0, 'testLast33', 'root', 'fictif33');
INSERT INTO USER values(34, 'fictif34@yahoo.fr', 1, 'testFirst34', 0, 'testLast34', 'root', 'fictif34');
INSERT INTO USER values(35, 'fictif35@yahoo.fr', 1, 'testFirst35', 0, 'testLast35', 'root', 'fictif35');
INSERT INTO USER values(36, 'fictif36@yahoo.fr', 1, 'testFirst36', 0, 'testLast36', 'root', 'fictif36');
INSERT INTO USER values(37, 'fictif37@yahoo.fr', 1, 'testFirst37', 0, 'testLast37', 'root', 'fictif37');
INSERT INTO USER values(38, 'fictif38@yahoo.fr', 1, 'testFirst38', 0, 'testLast38', 'root', 'fictif38');
INSERT INTO USER values(39, 'fictif39@yahoo.fr', 1, 'testFirst39', 0, 'testLast39', 'root', 'fictif39');


INSERT INTO `item` (`id`, `can_use`, `category`, `created_at`, `description`, `name`, `updated_at`, `url_image`, `use_description`) VALUES
(1, b'0', 'EQUIPEMENT', '2024-05-27 15:24:53.000000', 'Une faux forger dans un acier impeccable', 'Soul Eater', NULL, NULL, NULL),
(2, b'1', 'CONSOMMABLE', '2024-05-27 15:24:53.000000', 'Elixir', 'Elixir', NULL, NULL, 'Elixir'),
(3, b'1', 'ARTEFACT', '2024-05-27 15:24:53.000000', 'le seul et unique anneau', 'Anneau', NULL, NULL, 'le porteur devient invisible');


INSERT INTO `inventory` (`id`, `quantity`, `character_id`, `item_id`) VALUES
(1, 1, 252, 1),
(2, 6, 252, 2),
(3, 2, 253, 2);

INSERT INTO `shop` (`id`, `price`, `quantity`, `item_id`, `seller_id`) VALUES
(3, 1000000000000000, 1, 2, 452),
(8, 10000, 2, 2, 452),
(9, 9999, 1, 3, 452);

*/