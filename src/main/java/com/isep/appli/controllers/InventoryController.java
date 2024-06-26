package com.isep.appli.controllers;


import com.isep.appli.dbModels.Inventory;
import com.isep.appli.dbModels.Item;
import com.isep.appli.dbModels.Personnage;
import com.isep.appli.dbModels.Shop;
import com.isep.appli.models.enums.ItemCategory;
import com.isep.appli.services.InventoryService;
import com.isep.appli.services.ItemService;
import com.isep.appli.services.ShopService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ShopService shopService;
    @Autowired
    private ItemService itemService;


    /*******************************************************************************/
    /******************************** ALL ******************************************/
    /*******************************************************************************/


    /*******************************************************************************/
    /******************************** GUEST ****************************************/
    /*******************************************************************************/


    /*******************************************************************************/
    /******************************** PLAYER ***************************************/
    /*******************************************************************************/

    @GetMapping("/inventory")
    public String inventory(Model model, HttpSession session) {
        if (session.getAttribute("user") == null || session.getAttribute("personnage") == null) {
            return "errors/error-401";
        }
        Personnage character = (Personnage) session.getAttribute("personnage");
        List<Inventory> playerInventory = inventoryService.getPlayerInventory(character);
        model.addAttribute("personnage", character);
        model.addAttribute("item", new Item());
        return "inventory/inventory";
    }

    @PostMapping("inventory/findItem")
    public String searchItem(@Valid Item item, Model model, HttpSession session) {
        Personnage character = (Personnage) session.getAttribute("personnage");

        ItemCategory itemCategory = item.getCategory();
        String itemName = item.getName();
        if (!itemName.equals("") && itemCategory != null) {
           List<Inventory> playerInventory = inventoryService.getPlayerInventoryByItemNameAndItemCategory(character, item);
            model.addAttribute("playerInventory", playerInventory);
        } else if (!itemName.equals("") && itemCategory == null) {
            List<Inventory> playerInventory = inventoryService.getPlayerInventoryByItemName(character, item);
            model.addAttribute("playerInventory", playerInventory);
        } else if (itemName.equals("") && itemCategory != null) {
            List<Inventory> playerInventory = inventoryService.getPlayerInventoryByItemCategory(character, item);
            model.addAttribute("playerInventory", playerInventory);
        } else {
            List<Inventory> playerInventory = inventoryService.getPlayerInventory(character);
            model.addAttribute("playerInventory", playerInventory);
        }
        return "inventory/inventoryItem";
    }

    @PostMapping("/inventory/sellItem")
    public String sellItem(@Valid Shop shop, @RequestParam(name= "itemId") long itemId, HttpSession session) {
        Personnage character = (Personnage) session.getAttribute("personnage");
        Item item = itemService.findById(itemId);
        Inventory inventory = inventoryService.findByItemId(character, item);
        inventoryService.removeItemInInventory(inventory, shop.getQuantity());
        shop.setSeller(character);
        shop.setItem(item);
        shopService.save(shop);
        return "redirect:/inventory";
    }

    @GetMapping("/inventory/{idInventory}")
    public String showSellItem(@PathVariable long idInventory, Model model) {
        Inventory inventory = inventoryService.findById(idInventory).get(0);
        model.addAttribute("inventoryCell", inventory);
        model.addAttribute("shop", new Shop());
        Item item = inventory.getItem();
        System.out.println("Item = " + item);
        return "inventory/sellPanel";
    }
}