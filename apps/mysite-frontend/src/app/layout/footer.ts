import { Component } from '@angular/core';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'mysite-footer',
  imports: [FaIconComponent, RouterLink],
  templateUrl: './footer.html',
})
export class Footer {}
