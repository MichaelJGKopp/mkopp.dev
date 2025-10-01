import { bootstrapApplication } from '@angular/platform-browser';
import { appConfigClient } from './app/app.config.client';
import { App } from './app/app';

bootstrapApplication(App, appConfigClient).catch((err) => console.error(err));
