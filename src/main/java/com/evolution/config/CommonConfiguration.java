package com.evolution.config;

import com.cupboard.config.CupboardConfig;
import com.cupboard.config.ICommonConfig;
import com.google.gson.JsonObject;

import static com.evolution.Evolution.MOD_ID;

public class CommonConfiguration implements ICommonConfig
{
    public static CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(MOD_ID, new CommonConfiguration());

    public boolean displayTraitsInJadeOrWaila = true;
    public double noTraitWeight = 2.0;
    public int maxTraits = 3;

    public CommonConfiguration()
    {
    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty("desc:", "Whether display the traits on a mob in jade/waila(has to be installed to show): default:true");
        entry.addProperty("displayTraitsInJadeOrWaila", displayTraitsInJadeOrWaila);
        root.add("displayTraitsInJadeOrWaila", entry);

        final JsonObject entry2 = new JsonObject();
        entry2.addProperty("desc:", "Set the weight of getting no trait, the higher this is the lower the probability of received a trait for the entity. default:2.0");
        entry2.addProperty("noTraitWeight", noTraitWeight);
        root.add("noTraitWeight", entry2);

        final JsonObject entry3 = new JsonObject();
        entry3.addProperty("desc:", "Set maximum amount of traits that can be rolled. default:3");
        entry3.addProperty("maxTraits", maxTraits);
        root.add("maxTraits", entry3);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        displayTraitsInJadeOrWaila = data.get("displayTraitsInJadeOrWaila").getAsJsonObject().get("displayTraitsInJadeOrWaila").getAsBoolean();
        noTraitWeight = data.get("noTraitWeight").getAsJsonObject().get("noTraitWeight").getAsDouble();
        maxTraits = data.get("maxTraits").getAsJsonObject().get("maxTraits").getAsInt();
    }
}
