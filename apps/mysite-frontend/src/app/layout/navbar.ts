import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FaIconComponent } from '@fortawesome/angular-fontawesome';
import { faBars, faMoon, faSun } from '@fortawesome/free-solid-svg-icons';
@Component({
  selector: 'mysite-navbar',
  imports: [RouterLink, FaIconComponent],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {
  faBars = faBars;
  faSun = faSun;
  faMoon = faMoon;
}
