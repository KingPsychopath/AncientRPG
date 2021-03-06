package com.ancientshores.AncientRPG.Classes.Spells.Commands;

import com.ancientshores.AncientRPG.Classes.Spells.CommandDescription;
import com.ancientshores.AncientRPG.Classes.Spells.ParameterType;
import org.bukkit.Bukkit;

public class PrintToConsoleCommand extends ICommand {
    @CommandDescription(description = "<html>Logs the message to the server console</html>",
            argnames = {"message"}, name = "PrintToConsole", parameters = {ParameterType.String})
    public PrintToConsoleCommand() {
        this.paramTypes = new ParameterType[]{ParameterType.String};
    }

    @Override
    public boolean playCommand(EffectArgs ca) {
        if (ca.getParams().size() == 1 && ca.getParams().get(0) instanceof String) {
            String s = (String) ca.getParams().get(0);
            Bukkit.getServer().getConsoleSender().sendRawMessage(s);
        }
        return true;
    }
}