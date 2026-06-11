import * as path from 'path';

import * as express from 'express';
import * as nunjucks from 'nunjucks';

export class Nunjucks {
  constructor(public developmentMode: boolean) {
    this.developmentMode = developmentMode;
  }

  enableFor(app: express.Express): void {
    app.set('view engine', 'njk');
    const environment = nunjucks.configure(path.join(__dirname, '..', '..', 'views'), {
      autoescape: true,
      watch: this.developmentMode,
      express: app,
    });

    // Add custom filters for date formatting
    environment.addFilter('dateFilter', (dateString: string) => {
      if (!dateString) return 'N/A';
      const date = new Date(dateString);
      return date.toLocaleDateString('en-GB', { year: 'numeric', month: 'long', day: 'numeric' });
    });

    environment.addFilter('datetimeDisplay', (dateString: string) => {
      if (!dateString) return 'N/A';
      const date = new Date(dateString);
      const datePart = date.toLocaleDateString('en-GB', { year: 'numeric', month: 'long', day: 'numeric' });
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${datePart} at ${hours}:${minutes}`;
    });

    environment.addFilter('dateDay', (dateString: string) => {
      if (!dateString) return '';
      return String(new Date(dateString).getDate());
    });

    environment.addFilter('dateMonth', (dateString: string) => {
      if (!dateString) return '';
      return String(new Date(dateString).getMonth() + 1);
    });

    environment.addFilter('dateYear', (dateString: string) => {
      if (!dateString) return '';
      return String(new Date(dateString).getFullYear());
    });

    environment.addFilter('timeHHMM', (dateString: string) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${hours}:${minutes}`;
    });

    environment.addFilter('datetimeFormat', (dateString: string) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day}T${hours}:${minutes}`;
    });

    app.use((req, res, next) => {
      res.locals.pagePath = req.path;
      next();
    });
  }
}
