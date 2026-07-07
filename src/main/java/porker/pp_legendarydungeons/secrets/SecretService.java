package porker.pp_legendarydungeons.secrets;

import porker.pp_legendarydungeons.secrets.rayquaza.RayquazaSecretProgression;

public final class SecretService {
    private SecretService() {
    }

    public static void checkSecrets(SecretContext context) {
        RayquazaSecretProgression.check(context);
    }
}