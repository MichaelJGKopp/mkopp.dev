import { Component } from '@angular/core';
import { FaIconComponent } from "@fortawesome/angular-fontawesome";

@Component({
  selector: 'mysite-home.component',
  imports: [FaIconComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {}
