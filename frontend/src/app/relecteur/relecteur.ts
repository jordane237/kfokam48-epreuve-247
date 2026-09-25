import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { RelecturesApiService, RelectureEnAttente, RelectureRendue } from '../api/relectures.service';
import { EtudiantsApiService } from '../api/etudiants.service';
import { ApiError } from '../api/api-error';

/**
 * Écran Relecteur (F2) : les relectures assignées, formulaire note 0–20 + commentaire,
 * retours clairs sur 403 (RG7) et 409 (RG10). Une fois rendue, la relecture
 * disparaît de la liste — plus modifiable côté UI (RG10, cohérent avec l'API).
 */
@Component({
  selector: 'app-relecteur',
  imports: [FormsModule, DatePipe],
  templateUrl: './relecteur.html',
  styleUrl: './relecteur.css'
})
export class Relecteur implements OnInit {

  private readonly relecturesApi = inject(RelecturesApiService);
  private readonly etudiantsApi = inject(EtudiantsApiService);

  promotionId = 1;

  // --- Choix de l'identité (même mécanique Q1) ---
  etudiants = signal<{ id: number; nom: string }[]>([]);
  etudiantId: number | null = null;

  // --- Relectures à faire ---
  enAttente = signal<RelectureEnAttente[]>([]);
  chargement = signal(false);
  erreurChargement = signal<ApiError | null>(null);

  // --- Rendre une relecture ---
  exerciceChoisi = signal<RelectureEnAttente | null>(null);
  note: number | null = null;
  commentaire = '';
  renduEnCours = signal(false);
  erreurRendu = signal<ApiError | null>(null);
  confirmation = signal<RelectureRendue | null>(null);

  ngOnInit(): void {
    this.etudiantsApi.lister(this.promotionId).subscribe({
      next: (liste) => this.etudiants.set(liste),
      error: () => this.etudiants.set([])
    });
  }

  charger(): void {
    if (this.etudiantId === null) {
      this.erreurChargement.set({ code: 'CHAMP_MANQUANT', message: 'Choisissez votre nom.' });
      return;
    }
    this.chargement.set(true);
    this.erreurChargement.set(null);
    this.relecturesApi.enAttente(this.etudiantId).subscribe({
      next: (liste) => {
        this.enAttente.set(liste);
        this.chargement.set(false);
      },
      error: (err: ApiError) => {
        this.erreurChargement.set(err);
        this.chargement.set(false);
      }
    });
  }

  choisir(r: RelectureEnAttente): void {
    this.exerciceChoisi.set(r);
    this.note = null;
    this.commentaire = '';
    this.erreurRendu.set(null);
    this.confirmation.set(null);
  }

  rendre(): void {
    const exercice = this.exerciceChoisi();
    if (exercice === null || this.note === null) {
      this.erreurRendu.set({ code: 'NOTE_INVALIDE', message: 'Choisissez une note entre 0 et 20.' });
      return;
    }
    this.renduEnCours.set(true);
    this.erreurRendu.set(null);
    // Étape 3 : on soumet SA propre affectation (relecteurId) — chaque relecteur
    // ne voit toujours que ses propres relectures dans la liste.
    this.relecturesApi.rendre(exercice.exerciceId, this.note, this.commentaire.trim(), this.etudiantId ?? undefined).subscribe({
      next: (rendue) => {
        this.confirmation.set(rendue);
        this.renduEnCours.set(false);
        // RG10 : définitive → l'exercice sort de la liste, plus modifiable côté UI.
        this.enAttente.set(this.enAttente().filter(r => r.exerciceId !== rendue.exerciceId));
        this.exerciceChoisi.set(null);
      },
      error: (err: ApiError) => {
        this.erreurRendu.set(err);
        this.renduEnCours.set(false);
      }
    });
  }
}
