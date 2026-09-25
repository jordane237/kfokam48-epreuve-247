import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    // Couche d'appel API dédiée (F3) : tout le HTTP passe par HttpClient,
    // fourni ici une seule fois — aucun fetch dispersé dans les composants.
    provideHttpClient(withFetch())
  ]
};
