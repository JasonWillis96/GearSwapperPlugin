package gearswap;

import com.google.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.MenuAction;
import net.runelite.client.input.KeyListener;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.input.KeyManager;
import net.runelite.api.InventoryID;
import net.runelite.client.game.ItemManager;
import net.runelite.api.ItemContainer;

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

@PluginDescriptor(
        name = "Gear Swap Hotkey",
        description = "Swap between your custom gear sets using F5/F6",
        tags = {"gear", "swap", "hotkey"}
)
public class GearSwapPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private KeyManager keyManager;

    @Inject
    private ItemManager itemManager;

    private KeyListener keyListener;

    // --- Custom Gear Sets ---
    private final int[] gearSet1 = {
            12002, // Occult Necklace
            13740, // Imbued Saradomin Cape
            12904, // Trident of the Swamp (e)
            12938, // Malediction Ward
            4716,  // Ahrim's Robetop
            4720,  // Ahrim's Robeskirt
            12831  // Eternal Boots
    };

    private final int[] gearSet2 = {
            12006, // Necklace of Anguish
            10499, // Ava's Assembler
            12926, // Toxic Blowpipe
            1129,  // Black D'hide Body
            1099,  // Black D'hide Chaps
            11840  // Armadyl D'hide Boots
    };

    @Override
    protected void startUp() throws Exception
    {
        keyListener = new KeyListener()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_F5)
                {
                    swapGearSet(gearSet1);
                }
                else if (e.getKeyCode() == KeyEvent.VK_F6)
                {
                    swapGearSet(gearSet2);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}

            @Override
            public void keyTyped(KeyEvent e) {}
        };

        keyManager.registerKeyListener(keyListener);
    }

    @Override
    protected void shutDown() throws Exception
    {
        keyManager.unregisterKeyListener(keyListener);
    }

    // --- Gear swapping logic ---
    private void swapGearSet(int[] gearSet)
    {
        new Thread(() -> {
            ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
            if (inventory == null) return;

            for (int itemId : gearSet)
            {
                if (!isEquipped(itemId))
                {
                    for (int i = 0; i < inventory.getItems().length; i++)
                    {
                        Item item = inventory.getItems()[i];
                        if (item != null && item.getId() == itemId)
                        {
                            equipItem(item, i);
                            try
                            {
                                TimeUnit.MILLISECONDS.sleep(200); // small delay between swaps
                            }
                            catch (InterruptedException ignored) {}
                            break; // move to next gear
                        }
                    }
                }
            }
        }).start();
    }

    private boolean isEquipped(int itemId)
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null) return false;

        for (Item item : equipment.getItems())
        {
            if (item != null && item.getId() == itemId)
            {
                return true;
            }
        }
        return false;
    }

    private void equipItem(Item item, int slot)
    {
        // OpenOSRS allows invokeMenuAction
        String itemName = itemManager.getItemComposition(item.getId()).getName();
        client.invokeMenuAction(
                "Wear",
                itemName,
                slot,
                MenuAction.ITEM_FIRST_OPTION.getId(),
                item.getId(),
                9764864
        );
    }
}
