import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  private readonly apiUrl = '/api/employee';

  constructor(private http: HttpClient) {
  }

  generateMonthlyReport(employeeId: number, year: number, month: number): Observable<Blob> {
    const params = new HttpParams()
      .set('year', year)
      .set('month', month);

    return this.http.get(`${this.apiUrl}/${employeeId}/reports/monthly`, {
      params,
      responseType: 'blob'
    });
  }
}