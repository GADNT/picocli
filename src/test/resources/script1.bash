#!bash
#
# script1 Bash Completion
# =======================
#
# Bash completion support for script1,
# generated by [picocli](http://picocli.info/).
#
# Installation
# ------------
#
# 1. Place it in a `bash-completion.d` folder:
#
#   * /etc/bash-completion.d
#   * /usr/local/etc/bash-completion.d
#   * ~/bash-completion.d
#
# 2. Open new bash, and type `script1 [TAB][TAB]`
#
# Documentation
# -------------
# The script is called by bash whenever [TAB] or [TAB][TAB] is pressed after
# 'script1 (..)'. By reading entered command line parameters, it determines possible
# bash completions and writes them to the COMPREPLY variable. Bash then
# completes the user input if only one entry is listed in the variable or
# shows the options if more than one is listed in COMPREPLY.
#
# The script first determines the current parameter ($cur), the previous
# parameter ($prev), the first word ($firstword) and the last word ($lastword).
# Using the $firstword variable (= the command) and a giant switch/case,
# completions are written to $complete_words and $complete_options.
#
# If the current user input ($cur) starts with '-', only $command_options are
# displayed/completed, otherwise only $command_words.
#
# References
# ----------
# [1] http://stackoverflow.com/a/12495480/1440785
# [2] http://tiswww.case.edu/php/chet/bash/FAQ
#

shopt -s progcomp
_script1() {
    local cur prev firstword lastword complete_words complete_options

    # Don't break words at : and =, see [1] and [2]
    COMP_WORDBREAKS=${COMP_WORDBREAKS//[:=]}

    cur=${COMP_WORDS[COMP_CWORD]}
    prev=${COMP_WORDS[COMP_CWORD-1]}
    firstword=$(_get_firstword)
    lastword=$(_get_lastword)

    _OPTIONS="\
        -t\
        --timeout\
        --timeUnit\
        -u"

}

# Determines the first non-option word of the command line. This is usually the command.
_get_firstword() {
    local firstword i
    firstword=
    for ((i = 1; i < ${#COMP_WORDS[@]}; ++i)); do
        if [[ ${COMP_WORDS[i]} != -* ]]; then
            firstword=${COMP_WORDS[i]}
            break
        fi
    done
    echo $firstword
}

# Determines the last non-option word of the command line. This is usally a sub-command.
_get_lastword() {
    local lastword i
    lastword=
    for ((i = 1; i < ${#COMP_WORDS[@]}; ++i)); do
        if [[ ${COMP_WORDS[i]} != -* ]] && [[ -n ${COMP_WORDS[i]} ]] && [[ ${COMP_WORDS[i]} != $cur ]]; then
            lastword=${COMP_WORDS[i]}
        fi
    done
    echo $lastword
}

complete -F _script1 script1