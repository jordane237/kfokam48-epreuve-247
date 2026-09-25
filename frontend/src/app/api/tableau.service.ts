import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApiServiceBase } from './api-service-base';

/** Ligne du tableau — exactement le contrat. moyenne vaut null si aucune note. */
export interface LigneTableau {
  etudiantId: number;
  nom: string;
  presences: number;
  exercicesDeposes: number;
  moyenne: number | null;
  relecturesEnAttente: number;
}

/** Service dédié au tableau du formateur (F3). */
@Injectable({ providedIn: 'root' })
export class TableauApiService extends ApiServiceBase {
  private readonly http = inject(HttpClient);

  /** EF13 : le tableau complet ; la moyenne est déjà calculée par l'API — F3. */
  charger(promotionId: number) {
    return this.envelopper(
      this.http.get<LigneTableau[]>(`${this.apiUrl}/api/tableau`, {
        params: { promotionId: String(promotionId) }
      })
    );
  }
}
