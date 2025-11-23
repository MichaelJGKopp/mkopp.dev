export * from './blog.service';
import { BlogService } from './blog.service';
export * from './comments.service';
import { CommentsService } from './comments.service';
export * from './likes.service';
import { LikesService } from './likes.service';
export * from './user.service';
import { UserService } from './user.service';
export const APIS = [BlogService, CommentsService, LikesService, UserService];
