import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { SessionsApiService, SessionCreee } from '../api/sessions.service';
import { TableauApiService, LigneTableau } from '../api/tableau.service';
import { ApiError } from '../api/api-error';

/**
 * Écran Formateur (F2) : ouvrir une session (affiche le code) + tableau récapitulatif.
 * F3 : états de chargement et d'erreur gérés via signals ; la moyenne affichée
 * vient telle quelle de l'API — jamais recalculée ici.
 */
@Component({
  selector: 'app-formateur',
  imports: [FormsModule, DatePipe],
  templateUrl: './formateur.html',
  styleUrl: './formateur.css'
})
export class Formateur {

  private readonly sessionsApi = inject(SessionsApiService);
  private readonly tableauApi = inject(TableauApiService);

  // --- Ouverture de session ---
  titre = '';
  promotionId = 1;
  ouvertureEnCours = signal(false);
  erreurOuverture = signal<ApiError | null>(null);
  sessionCreee = signal<SessionCreee | null>(null);

  // --- Tableau ---
  lignes = signal<LigneTableau[]>([]);
  tableauEnChargement = signal(false);
  erreurTableau = signal<ApiError | null>(null);

  ouvrirSession(): void {
    if (!this.titre.trim()) {
      this.erreurOuverture.set({ code: 'CHAMP_MANQUANT', message: 'Le titre est obligatoire.' });
      return;
    }
    this.ouvertureEnCours.set(true);
    this.erreurOuverture.set(null);
    this.sessionCreee.set(null);
    this.sessionsApi.ouvrir(this.titre.trim(), this.promotionId).subscribe({
      next: (creee) => {
        this.sessionCreee.set(creee);
        this.ouvertureEnCours.set(false);
        this.titre = '';
      },
      error: (err: ApiError) => {
        this.erreurOuverture.set(err);
        this.ouvertureEnCours.set(false);
      }
    });
  }

  chargerTableau(): void {
    this.tableauEnChargement.set(true);
    this.erreurTableau.set(null);
    this.tableauApi.charger(this.promotionId).subscribe({
      next: (lignes) => {
        this.lignes.set(lignes);
        this.tableauEnChargement.set(false);
      },
      error: (err: ApiError) => {
        this.erreurTableau.set(err);
        this.tableauEnChargement.set(false);
      }
    });
  }
}
