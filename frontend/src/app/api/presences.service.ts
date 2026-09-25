import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApiServiceBase } from './api-service-base';

/** Sortie 201 de POST /api/presences — contrat. */
export interface PresenceCreee {
  id: number;
  sessionId: number;
  etudiantId: number;
  source: 'ETUDIANT' | 'FORMATEUR';
}

/** Service dédié aux présences (F3). */
@Injectable({ providedIn: 'root' })
export class PresencesApiService extends ApiServiceBase {
  private readonly http = inject(HttpClient);

  /** EF2 : marquer sa présence ; erreurs 400/409/410/429 normalisées par la base. */
  marquer(code: string, etudiantId: number) {
    return this.envelopper(
      this.http.post<PresenceCreee>(`${this.apiUrl}/api/presences`, { code, etudiantId })
    );
  }
}
