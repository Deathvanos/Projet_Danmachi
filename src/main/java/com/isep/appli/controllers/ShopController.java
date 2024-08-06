package com.isep.appli.controllers;

import com.isep.appli.dbModels.*;
import com.isep.appli.models.enums.ItemCategory;
import com.isep.appli.services.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ShopController {

    @Autowired
    private ShopService shopService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private PersonnageService personnageService;


    /*******************************************************************************/
    /******************************** ALL ******************************************/
    /*******************************************************************************/


    /*******************************************************************************/
    /******************************** GUEST ****************************************/
    /*******************************************************************************/


    /*******************************************************************************/
    /******************************** PLAYER ***************************************/
    /*******************************************************************************/

    @GetMapping("/shop")
    public String shop(Model model, HttpSession session) {
        Personnage character = (Personnage) session.getAttribute("personnage");
        if (session.getAttribute("user") == null || session.getAttribute("personnage") == null) {
            return "errors/error-401";
        }
        model.addAttribute("personnage", character);
        model.addAttribute("item", new Item());
        return "shop/shop";
    }

    @PostMapping("/shop/findItem")
    public String searchItem(@RequestParam(value = "name", required = false) String itemName,
                             @RequestParam(value = "category", required = false) ItemCategory itemCategory,
                             @RequestParam(value = "minPrice", required = false) Long minPrice,
                             @RequestParam(value = "maxPrice", required = false) Long maxPrice,
                             Model model) {
        Item item = new Item();
        item.setName(itemName);
        item.setCategory(itemCategory);
        List<Shop> allShopWithFilter = shopService.getAll();
        List<Shop> allRemoveShop;
        if (!itemName.equals("")) {
            allRemoveShop = shopService.findAllShopWhereItemNameIsNot(allShopWithFilter, item);
            allShopWithFilter.removeAll(allRemoveShop);
        }
        if (itemCategory != null) {
            allRemoveShop = shopService.findAllShopWhereItemCategoryIsNot(allShopWithFilter, item);
            allShopWithFilter.removeAll(allRemoveShop);
        }
        if(minPrice != null){
            allRemoveShop = shopService.findAllShopWhereMinPriceIsNot(allShopWithFilter, minPrice);
            allShopWithFilter.removeAll(allRemoveShop);
        }
        if(maxPrice != null){
            allRemoveShop = shopService.findAllShopWhereMaxPriceIsNot(allShopWithFilter, maxPrice);
            allShopWithFilter.removeAll(allRemoveShop);
        }
        model.addAttribute("allShop", allShopWithFilter);
        return "shop/shopItem";
    }

    @PostMapping("/shop/buyItem")
    public String sellItem(@Valid Shop shopCell, HttpSession session) {

        Shop shop = shopService.findById(shopCell.getId());
        int itemQuantityToBuy = shopCell.getQuantity();
        Item item = shop.getItem();

        shopService.removeItemInShop(shop, itemQuantityToBuy);

        Personnage seller = shop.getSeller();
        int currentMoneyForSeller = seller.getMoney();
        int price = Long.valueOf(shopCell.getPrice()).intValue();
        personnageService.updateMoney(seller, currentMoneyForSeller + price );

        Personnage currentPersonnage = (Personnage) session.getAttribute("personnage");
        Personnage character = personnageService.getPersonnageById(currentPersonnage.getId());
        int currentMoneyForCharacter = character.getMoney();
        personnageService.updateMoney(character, currentMoneyForCharacter - price);
        inventoryService.addItemForPlayer(character, item, itemQuantityToBuy);

        return "redirect:/shop";
    }

    @GetMapping("/shop/{idShop}")
    public String showSellItem(@PathVariable long idShop, Model model, HttpSession session) {
        Shop shop = shopService.findById(idShop);
        Personnage personnage = (Personnage) session.getAttribute("personnage");
        int maxYouCanBuy = (int) (personnage.getMoney() / shop.getPrice());
        if(maxYouCanBuy < shop.getQuantity()){
            shop.setQuantity(maxYouCanBuy);
        }
        model.addAttribute("shopCell", shop);

        return "shop/buyPanel";
    }
}