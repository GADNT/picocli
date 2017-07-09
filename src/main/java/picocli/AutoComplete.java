/*
   Copyright 2017 Remko Popma

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package picocli;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a bash auto-complete script.
 */
public class AutoComplete {

    private AutoComplete() {
    }

    private static final String HEADER = "" +
            "#!bash\n" +
            "#\n" +
            "# %1$s Bash Completion\n" +
            "# =======================\n" +
            "#\n" +
            "# Bash completion support for %1$s,\n" +
            "# generated by [picocli](http://picocli.info/).\n" +
            "#\n" +
            "# Installation\n" +
            "# ------------\n" +
            "#\n" +
            "# 1. Place it in a `bash-completion.d` folder:\n" +
            "#\n" +
            "#   * /etc/bash-completion.d\n" +
            "#   * /usr/local/etc/bash-completion.d\n" +
            "#   * ~/bash-completion.d\n" +
            "#\n" +
            "# 2. Open new bash, and type `%1$s [TAB][TAB]`\n" +
            "#\n" +
            "# Documentation\n" +
            "# -------------\n" +
            "# The script is called by bash whenever [TAB] or [TAB][TAB] is pressed after\n" +
            "# '%1$s (..)'. By reading entered command line parameters, it determines possible\n" +
            "# bash completions and writes them to the COMPREPLY variable. Bash then\n" +
            "# completes the user input if only one entry is listed in the variable or\n" +
            "# shows the options if more than one is listed in COMPREPLY.\n" +
            "#\n" +
            "# The script first determines the current parameter ($cur), the previous\n" +
            "# parameter ($prev), the first word ($firstword) and the last word ($lastword).\n" +
            "# Using the $firstword variable (= the command) and a giant switch/case,\n" +
            "# completions are written to $complete_words and $complete_options.\n" +
            "#\n" +
            "# If the current user input ($cur) starts with '-', only $command_options are\n" +
            "# displayed/completed, otherwise only $command_words.\n" +
            "#\n" +
            "# References\n" +
            "# ----------\n" +
            "# [1] http://stackoverflow.com/a/12495480/1440785\n" +
            "# [2] http://tiswww.case.edu/php/chet/bash/FAQ\n" +
            "#\n" +
            "\n" +
            "shopt -s progcomp\n" +
            "_%1$s() {\n" +
            "    local cur prev firstword lastword complete_words complete_options\n" +
            "\n" +
            "    # Don't break words at : and =, see [1] and [2]\n" +
            "    COMP_WORDBREAKS=${COMP_WORDBREAKS//[:=]}\n" +
            "\n" +
            "    cur=${COMP_WORDS[COMP_CWORD]}\n" +
            "    prev=${COMP_WORDS[COMP_CWORD-1]}\n" +
            "    firstword=$(_get_firstword)\n" +
            "    lastword=$(_get_lastword)\n" +
            "\n";

    private static final String FOOTER = "}\n\n" +
            "# Determines the first non-option word of the command line. This is usually the command.\n" +
            "_get_firstword() {\n" +
            "    local firstword i\n" +
            "    firstword=\n" +
            "    for ((i = 1; i < ${#COMP_WORDS[@]}; ++i)); do\n" +
            "        if [[ ${COMP_WORDS[i]} != -* ]]; then\n" +
            "            firstword=${COMP_WORDS[i]}\n" +
            "            break\n" +
            "        fi\n" +
            "    done\n" +
            "    echo $firstword\n" +
            "}\n" +
            "\n" +
            "# Determines the last non-option word of the command line. This is usally a sub-command.\n" +
            "_get_lastword() {\n" +
            "    local lastword i\n" +
            "    lastword=\n" +
            "    for ((i = 1; i < ${#COMP_WORDS[@]}; ++i)); do\n" +
            "        if [[ ${COMP_WORDS[i]} != -* ]] && [[ -n ${COMP_WORDS[i]} ]] && [[ ${COMP_WORDS[i]} != $cur ]]; then\n" +
            "            lastword=${COMP_WORDS[i]}\n" +
            "        fi\n" +
            "    done\n" +
            "    echo $lastword\n" +
            "}\n";

    public static String bash(String scriptName, CommandLine commandLine) {
        if (scriptName == null)  { throw new NullPointerException("scriptName"); }
        if (commandLine == null) { throw new NullPointerException("commandLine"); }
        String result = "";
        result += String.format(HEADER, scriptName);
        result += generateAutoComplete("_", commandLine);
        return result + FOOTER;
    }

    private static String generateAutoComplete(String prefix, CommandLine commandLine) {
        Object annotated = commandLine.getCommand();
        List<Field> requiredFields = new ArrayList<Field>();
        Map<String, Field> optionName2Field = new HashMap<String, Field>();
        Map<Character, Field> singleCharOption2Field = new HashMap<Character, Field>();
        List<Field> positionalParameterFields = new ArrayList<Field>();
        Class<?> cls = annotated.getClass();
        while (cls != null) {
            CommandLine.init(cls, requiredFields, optionName2Field, singleCharOption2Field, positionalParameterFields);
            cls = cls.getSuperclass();
        }

        String result = "";
        result += optionDeclaration(prefix, optionName2Field);

        Map<String, CommandLine> commands = commandLine.getSubcommands();
        result += commandListDeclaration(prefix, commands);

        for (Map.Entry<String, CommandLine> entry : commands.entrySet()) {
            result += generateAutoComplete(prefix + entry.getKey() + "_", entry.getValue());
        }
        return result;
    }

    private static String optionDeclaration(String prefix, Map<String, Field> options) {
        if (options.isEmpty()) { return ""; }
        StringBuilder result = new StringBuilder("    ").append(prefix).append("OPTIONS=\"\\\n");
        for (String key : options.keySet()) {
            result.append("        ").append(key).append("\\\n");
        }
        result.setCharAt(result.length() - 2, '\"');
        return result.toString() + "\n";
    }

    private static String commandListDeclaration(String prefix, Map<String, CommandLine> commands) {
        if (commands.isEmpty()) { return ""; }
        StringBuilder result = new StringBuilder("    ").append(prefix).append("COMMANDS=\"\\\n");
        for (String key : commands.keySet()) {
            result.append("        ").append(key).append("\\\n");
        }
        result.setCharAt(result.length() - 2, '\"');
        return result.toString() + "\n";
    }
}