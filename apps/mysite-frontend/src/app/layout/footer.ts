import { Component } from '@angular/core';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';

@Component({
  selector: 'mysite-footer',
  imports: [FaIconComponent],
  templateUrl: './footer.html',
  styleUrl: './footer.scss',
})
export class Footer {}
