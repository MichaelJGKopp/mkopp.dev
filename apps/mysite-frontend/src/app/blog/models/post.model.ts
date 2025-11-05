export interface BlogPost {
  slug: string;
  title: string;
  description: string;
  content: string;
  publishedAt: Date;
  tags: string[];
  author: string;
  thumbnailUrl?: string;
  type: 'blog' | 'youtube' | 'external';
  externalUrl?: string;
}

export interface BlogPostMetadata {
  slug: string;
  title: string;
  description: string;
  publishedAt: string;
  tags: string[];
  author: string;
  thumbnailUrl?: string;
  type: 'blog' | 'youtube' | 'external';
  externalUrl?: string;
}
