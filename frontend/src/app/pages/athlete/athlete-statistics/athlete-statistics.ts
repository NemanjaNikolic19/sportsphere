import { AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Chart, registerables } from 'chart.js';
import { Statistics } from '../../../models/statistics';
import { AthleteService } from '../../../services/athlete.service';
import { AuthService } from '../../../services/auth.service';
import { RouterLink } from '@angular/router';

Chart.register(...registerables);

@Component({
  selector: 'app-athlete-statistics',
  imports: [RouterLink],
  templateUrl: './athlete-statistics.html',
  styleUrl: './athlete-statistics.css',
})
export class AthleteStatistics
    implements OnInit, AfterViewInit, OnDestroy {

  @ViewChild('sportChart')
  sportChartCanvas?:
    ElementRef<HTMLCanvasElement>;

  @ViewChild('monthlyChart')
  monthlyChartCanvas?:
    ElementRef<HTMLCanvasElement>;

  @ViewChild('spendingChart')
  spendingChartCanvas?:
    ElementRef<HTMLCanvasElement>;

  statistics: Statistics | null = null;

  loading = false;
  error = '';

  private viewInitialized = false;

  private sportChart: Chart | null = null;
  private monthlyChart: Chart | null = null;
  private spendingChart: Chart | null = null;

  constructor(
    private athleteService: AthleteService,
    private authService: AuthService
  ) {
  }

  ngOnInit(): void {
    this.loadStatistics();
  }

  ngAfterViewInit(): void {
    this.viewInitialized = true;
    this.renderChartsIfReady();
  }

  ngOnDestroy(): void {
    this.destroyCharts();
  }

  loadStatistics(): void {
    const user = this.authService.currentUser();

    if(
      user === null ||
      user.role !== 'ATHLETE'
    ) {
      this.error =
        'Only logged athletes can view statistics.';

      return;
    }

    this.loading = true;
    this.error = '';

    this.athleteService
      .getStatistics(user.id)
      .subscribe({
        next: statistics => {
          this.statistics = statistics;
          this.loading = false;

          this.renderChartsIfReady();
        },
        error: err => {
          this.statistics = null;
          this.loading = false;

          this.error =
            err.error?.message ||
            'Could not load statistics.';

          this.destroyCharts();
        }
      });
  }

  private renderChartsIfReady(): void {
    if(
      !this.viewInitialized ||
      this.statistics === null ||
      !this.sportChartCanvas ||
      !this.monthlyChartCanvas ||
      !this.spendingChartCanvas
    ) {
      return;
    }

    this.destroyCharts();

    this.createSportChart();
    this.createMonthlyChart();
    this.createSpendingChart();
  }

  private createSportChart(): void {
    if(
      !this.statistics ||
      !this.sportChartCanvas
    ) {
      return;
    }

    const labels =
      Object.keys(
        this.statistics.playedBySport
      );

    this.sportChart = new Chart(
      this.sportChartCanvas.nativeElement,
      {
        type: 'bar',

        data: {
          labels: labels,

          datasets: [
            {
              label: 'Played',
              data: labels.map(
                sport =>
                  this.statistics!
                    .playedBySport[sport]
              ),
              backgroundColor: '#16a34a'
            },
            {
              label: 'Reserved',
              data: labels.map(
                sport =>
                  this.statistics!
                    .reservedBySport[sport]
              ),
              backgroundColor: '#2563eb'
            }
          ]
        },

        options: {
          responsive: true,
          maintainAspectRatio: false,

          plugins: {
            title: {
              display: true,
              text: 'Terms by sport'
            }
          },

          scales: {
            y: {
              beginAtZero: true,

              ticks: {
                precision: 0
              }
            }
          }
        }
      }
    );
  }

  private createMonthlyChart(): void {
    if(
      !this.statistics ||
      !this.monthlyChartCanvas
    ) {
      return;
    }

    const labels =
      Object.keys(
        this.statistics.monthlyActivity
      );

    this.monthlyChart = new Chart(
      this.monthlyChartCanvas.nativeElement,
      {
        type: 'line',

        data: {
          labels: labels,

          datasets: [
            {
              label: 'Reservations',
              data: labels.map(
                month =>
                  this.statistics!
                    .monthlyActivity[month]
              ),
              borderColor: '#7c3aed',
              backgroundColor:
                'rgba(124, 58, 237, 0.15)',
              pointBackgroundColor: '#7c3aed',
              pointRadius: 4,
              tension: 0.25,
              fill: true
            }
          ]
        },

        options: {
          responsive: true,
          maintainAspectRatio: false,

          plugins: {
            title: {
              display: true,
              text: 'Activity during the last 12 months'
            }
          },

          scales: {
            y: {
              beginAtZero: true,

              ticks: {
                precision: 0
              }
            }
          }
        }
      }
    );
  }

  private createSpendingChart(): void {
    if(
      !this.statistics ||
      !this.spendingChartCanvas
    ) {
      return;
    }

    const labels =
      Object.keys(
        this.statistics
          .equipmentSpendingByAthlete
      );

    const colors = [
      '#2563eb',
      '#16a34a',
      '#7c3aed',
      '#ea580c',
      '#dc2626',
      '#0891b2'
    ];

    this.spendingChart = new Chart(
      this.spendingChartCanvas.nativeElement,
      {
        type: 'bar',

        data: {
          labels: labels,

          datasets: [
            {
              label: 'Equipment spending',
              data: labels.map(
                username =>
                  this.statistics!
                    .equipmentSpendingByAthlete[
                      username
                    ]
              ),
              backgroundColor: labels.map(
                (_, index) =>
                  colors[index % colors.length]
              )
            }
          ]
        },

        options: {
          indexAxis: 'y',
          responsive: true,
          maintainAspectRatio: false,

          plugins: {
            title: {
              display: true,
              text: 'Equipment spending by athlete'
            },

            legend: {
              display: false
            }
          },

          scales: {
            x: {
              beginAtZero: true
            }
          }
        }
      }
    );
  }

  private destroyCharts(): void {
    this.sportChart?.destroy();
    this.monthlyChart?.destroy();
    this.spendingChart?.destroy();

    this.sportChart = null;
    this.monthlyChart = null;
    this.spendingChart = null;
  }

  formatMoney(amount: number): string {
    return new Intl.NumberFormat(
      'sr-RS',
      {
        style: 'currency',
        currency: 'RSD'
      }
    ).format(amount);
  }
}