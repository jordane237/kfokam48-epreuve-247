import { Routes } from '@angular/router';

// Les trois écrans (F2) — chargés à la demande (lazy).
// T9 (étudiant) et T10 (relecteur) complèteront ce tableau.
export const routes: Routes = [
  { path: 'formateur', loadComponent: () => import('./formateur/formateur').then(m => m.Formateur) },
];
