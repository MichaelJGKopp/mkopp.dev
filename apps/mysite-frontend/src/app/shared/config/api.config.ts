import { Configuration } from '@mkopp/api-clients/backend';
import { environment } from '../../../environments/environment';

export function apiConfigFactory(): Configuration {
  return new Configuration({
    basePath: environment.API_URL,
  });
}
