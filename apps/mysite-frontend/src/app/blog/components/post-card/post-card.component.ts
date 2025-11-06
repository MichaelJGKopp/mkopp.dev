import { Component, inject, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { BlogPostMetadata } from '../../models/post.model';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { faCalendar, faTag, faArrowRight } from '@fortawesome/free-solid-svg-icons';
import { ThemeService } from '../../../shared/theme/theme.service';

@Component({
  selector: 'mysite-post-card',
  standalone: true,
  imports: [CommonModule, RouterModule, FontAwesomeModule],
  templateUrl: './post-card.component.html',
  styleUrls: ['./post-card.component.scss'],
})
export class PostCardComponent {
  @Input({ required: true }) post!: BlogPostMetadata;
  
  private themeService = inject(ThemeService);

  isDarkMode = this.themeService.isDarkMode;

  faCalendar = faCalendar;
  faTag = faTag;
  faArrowRight = faArrowRight;
}
