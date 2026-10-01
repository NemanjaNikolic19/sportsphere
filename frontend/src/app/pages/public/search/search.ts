import { Component, OnInit } from '@angular/core';
import { PublicSearchParams } from '../../../models/public-search-params';
import { Facility } from '../../../models/facility';
import { PublicService } from '../../../services/public.service';
import { Sport } from '../../../models/sport';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-search',
  imports: [FormsModule, RouterLink],
  templateUrl: './search.html',
  styleUrl: './search.css',
})
export class PublicSearch {

  cities: string[] = []
  sports: Sport[] = []

  sortField: 'name' | 'city' | 'sport' = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  facilitySportNames: Record<number, string> = {};

  resourceTypes = [
    { value: 'OUTDOOR_FIELD', label: 'Outdoor field'},
    { value: 'INDOOR_COURT', label: 'Indoor courd'},
    { value: 'HALL', label: 'Hall'}
  ];

  searchParams: PublicSearchParams = {
    name: '',
    city: '',
    sportId: null,
    resourceType: ''
  };

  facilities: Facility[] = [];

  loadingFilters = false;
  loadingSearch = false;
  searched = false;
  errorMessage = '';

  constructor(private publicService: PublicService) {

  }

  ngOnInit(): void {
    this.loadFilters();
  }

  loadFilters(): void {
    this.loadingFilters = true;
    this.errorMessage = '';

    this.publicService.getCities().subscribe({
      next: cities => {
        this.cities = cities;
        this.loadingFilters = false;
      },
      error: err => {
        this.loadingFilters = false;
        this.errorMessage = err.error?.message || 'Could not load cities';
      }
    });

    this.publicService.getSports().subscribe({
      next: sports => {
        this.sports = sports;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Could not load sports';
      }
    });
  }

  search(): void {
    this.loadingSearch = true;
    this.searched = true;
    this.errorMessage = '';

    this.publicService.searchFacilities(this.searchParams).subscribe({
      next: facilities => {
        this.facilities = facilities
        this.loadingSearch = false;

        this.facilitySportNames = {};

        for(const facility of facilities) {
          this.loadFacilitySportNames(facility.id);
        }
      },
      error: err => {
        this.loadingSearch = false;
        this.errorMessage =  err.error?.message || 'Search failed';
      }
    });
  }

  clear(): void {
    this.searchParams = {
      name: '',
      city: '',
      sportId: null,
      resourceType: ''
    };

    this.facilities = [];
    this.searched = false;
    this.errorMessage = '';
  }

  sortBy(field: 'name' | 'city' | 'sport'): void {
    if(this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
  }

  sortIcon(field: 'name' | 'city' | 'sport'): string {
    if(this.sortField !== field) {
      return '↕';
    }

    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  sortedFacilities(): Facility[] {
    return [...this.facilities].sort((a, b) => {
      const valueA = this.sortValue(a);
      const valueB = this.sortValue(b);

      const result = valueA.localeCompare(valueB, 'sr', {
        sensitivity: 'base'
      });

      return this.sortDirection === 'asc' ? result : -result;
    });
  }

  sortValue(facility: Facility): string {
    if(this.sortField === 'name') {
      return facility.name || '';
    }

    if(this.sortField === 'city') {
      return facility.city || '';
    }

    return this.sportNamesForFacility(facility.id);
  }

  loadFacilitySportNames(facilityId: number): void {
    this.publicService.getFacilityResources(facilityId).subscribe({
      next: resources => {
        const sportIds = Array.from(new Set(resources.map(resource => resource.sportId)));

        const names = sportIds
          .map(sportId => this.sportName(sportId))
          .sort((a, b) => a.localeCompare(b, 'sr', { sensitivity: 'base' }));

        this.facilitySportNames[facilityId] = names.join(', ');
      },
      error: () => {
        this.facilitySportNames[facilityId] = '-';
      }
    });
  }

  sportNamesForFacility(facilityId: number): string {
    return this.facilitySportNames[facilityId] || '-';
  }

  sportName(sportId: number): string {
    const sport = this.sports.find(s => s.id === sportId);

    return sport ? sport.name : `Sport #${sportId}`;
  }
}
