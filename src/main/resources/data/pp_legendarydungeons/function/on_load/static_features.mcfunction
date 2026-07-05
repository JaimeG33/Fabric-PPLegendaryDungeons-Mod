# Starts the central static feature scanner.
# This replaces directly loading/scheduling individual static features.
# This is to load in features that do not need to be constantly in the tick function and can afford a possible second delay before they are loaded.

schedule function pp_legendarydungeons:static_features 1t replace