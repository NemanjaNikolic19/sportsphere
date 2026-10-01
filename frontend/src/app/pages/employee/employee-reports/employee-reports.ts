import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../services/auth.service';
import { ReportService } from '../../../services/report.service';

@Component({
  selector: 'app-employee-reports',
  imports: [FormsModule, RouterLink],
  templateUrl: './employee-reports.html',
  styleUrl: './employee-reports.css'
})
export class EmployeeReports implements OnInit {

  employeeId: number | null = null;

  selectedYear = new Date().getFullYear();
  selectedMonth = new Date().getMonth() + 1;

  years: number[] = [];
  months = [
    { value: 1, label: 'January' },
    { value: 2, label: 'February' },
    { value: 3, label: 'March' },
    { value: 4, label: 'April' },
    { value: 5, label: 'May' },
    { value: 6, label: 'June' },
    { value: 7, label: 'July' },
    { value: 8, label: 'August' },
    { value: 9, label: 'September' },
    { value: 10, label: 'October' },
    { value: 11, label: 'November' },
    { value: 12, label: 'December' }
  ];

  generating = false;

  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private reportService: ReportService
  ) {
  }

  ngOnInit(): void {
    const currentUser = this.authService.currentUser();

    if(!currentUser) {
      this.errorMessage = 'You must be logged in.';
      return;
    }

    if(currentUser.role !== 'EMPLOYEE') {
      this.errorMessage = 'Only employees can access this page.';
      return;
    }

    this.employeeId = currentUser.id;

    this.initYears();
  }

  generateReport(): void {
    if(this.employeeId === null) {
      return;
    }

    if(!this.isFormValid()) {
      this.errorMessage = 'Choose valid year and month.';
      return;
    }

    this.generating = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.reportService.generateMonthlyReport(
      this.employeeId,
      this.selectedYear,
      this.selectedMonth
    ).subscribe({
      next: blob => {
        this.downloadBlob(blob);
        this.generating = false;
        this.successMessage = 'Report downloaded.';
      },
      error: err => {
        this.generating = false;
        this.handleReportError(err);
      }
    });
  }

  isFormValid(): boolean {
    return this.selectedYear >= 2000 &&
      this.selectedYear <= 2100 &&
      this.selectedMonth >= 1 &&
      this.selectedMonth <= 12;
  }

  selectedMonthLabel(): string {
    const month = this.months.find(m => m.value === this.selectedMonth);

    return month ? month.label : String(this.selectedMonth);
  }

  private initYears(): void {
    const currentYear = new Date().getFullYear();

    this.years = [];

    for(let year = currentYear - 5; year <= currentYear + 1; year++) {
      this.years.push(year);
    }
  }

  private downloadBlob(blob: Blob): void {
    const filename =
      `sportsphere-report-${this.selectedYear}-${String(this.selectedMonth).padStart(2, '0')}.pdf`;

    const url = window.URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();

    window.URL.revokeObjectURL(url);
  }

  private handleReportError(err: any): void {
    if(err.error instanceof Blob) {
      err.error.text().then((text: string) => {
        try {
          const parsed = JSON.parse(text);
          this.errorMessage = parsed.message || 'Could not generate report.';
        } catch {
          this.errorMessage = 'Could not generate report.';
        }
      });

      return;
    }

    this.errorMessage = err.error?.message || 'Could not generate report.';
  }
}