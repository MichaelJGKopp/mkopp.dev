import { Component, inject, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FaConfig, FaIconComponent, FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { fontAwesomeIcons } from './shared/font-awesome-icons';
import { Navbar } from "./layout/navbar";
import { Footer } from "./layout/footer";

@Component({
  imports: [RouterModule, Navbar, Footer, FaIconComponent],
  selector: 'mysite-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  protected title = 'mysite-frontend';
  private faIconLibrary = inject(FaIconLibrary);
  private faConfig = inject(FaConfig);

  ngOnInit() {
    this.initFontAwesome();
  }

  private initFontAwesome() {
    this.faConfig.defaultPrefix = 'far';  // font awesome regular 1 of 3 bundles
    this.faIconLibrary.addIcons(...fontAwesomeIcons); // add any icons you want to use in the app here
  }
}
