import { Component, OnInit, AfterViewInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../../../services/event.service';
import { ImageUploadService } from '../../../services/image-upload.service';
import * as L from 'leaflet';

const iconDefault = L.icon({
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
  shadowSize: [41, 41]
});
L.Marker.prototype.options.icon = iconDefault;

@Component({
  selector: 'app-event-form',
  templateUrl: './event-form.html'
})
export class EventFormComponent implements OnInit, AfterViewInit {
  eventForm: FormGroup;
  isEditMode = false;
  eventId: number | null = null;
  clubId: number | null = null;
  loading = false;
  saving = false;
  uploading = false;
  imageMode: 'url' | 'upload' = 'url';

  selectedLat: number | null = null;
  selectedLng: number | null = null;
  selectedLocationName = '';
  private map!: L.Map;
  private marker: L.Marker | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private imageUploadService: ImageUploadService
  ) {
    this.eventForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      eventDate: ['', Validators.required],
      location: ['', Validators.required],
      imageUrl: [''],
      paid: [false],
      price: [null],
      maxParticipants: [null]
    });
  }

  ngOnInit(): void {
    const clubId = this.route.snapshot.paramMap.get('id');
    const eventId = this.route.snapshot.paramMap.get('id');
    const url = this.route.snapshot.url.map(s => s.path).join('/');

    if (url.includes('create-event')) {
      this.clubId = Number(clubId);
    } else if (url.includes('edit')) {
      this.isEditMode = true;
      this.eventId = Number(eventId);
      this.loading = true;
      this.eventService.getEventById(this.eventId).subscribe({
        next: data => {
          this.clubId = data.clubId;
          this.selectedLat = data.latitude || null;
          this.selectedLng = data.longitude || null;
          this.selectedLocationName = data.locationName || '';
          this.eventForm.patchValue({
            title: data.title,
            description: data.description,
            eventDate: data.eventDate ? new Date(data.eventDate).toISOString().slice(0, 16) : '',
            location: data.location,
            imageUrl: data.imageUrl,
            paid: data.paid || false,
            price: data.price || null,
            maxParticipants: data.maxParticipants || null
          });
          this.loading = false;
          setTimeout(() => this.initMap(), 100);
        },
        error: () => { this.loading = false; }
      });
    }
  }

  ngAfterViewInit(): void {
    if (!this.isEditMode) {
      setTimeout(() => this.initMap(), 0);
    }
  }

  private initMap(): void {
    const mapEl = document.getElementById('eventMap');
    if (!mapEl || this.map) return;

    this.map = L.map('eventMap').setView([36.8065, 10.1815], 7);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    if (this.selectedLat && this.selectedLng) {
      this.placeMarker(this.selectedLat, this.selectedLng);
    }

    this.map.on('click', (e: L.LeafletMouseEvent) => {
      this.placeMarker(e.latlng.lat, e.latlng.lng);
      this.reverseGeocode(e.latlng.lat, e.latlng.lng);
    });
  }

  private placeMarker(lat: number, lng: number): void {
    if (this.marker) {
      this.map.removeLayer(this.marker);
    }
    this.marker = L.marker([lat, lng]).addTo(this.map);
    this.selectedLat = lat;
    this.selectedLng = lng;
  }

  private reverseGeocode(lat: number, lng: number): void {
    fetch(`https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json`)
      .then(r => r.json())
      .then((data: any) => {
        const name = data.display_name || `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
        this.selectedLocationName = name;
        if (!this.eventForm.value.location) {
          this.eventForm.patchValue({ location: name });
        }
      })
      .catch(() => {
        this.selectedLocationName = `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
      });
  }

  get isPaid(): boolean {
    return !!this.eventForm.value.paid;
  }

  onSubmit() {
    if (this.eventForm.valid) {
      this.saving = true;
      const data = {
        ...this.eventForm.value,
        clubId: this.clubId,
        latitude: this.selectedLat,
        longitude: this.selectedLng,
        locationName: this.selectedLocationName || null
      };

      if (this.isEditMode && this.eventId) {
        this.eventService.updateEvent(this.eventId, data).subscribe({
          next: () => { this.router.navigate(['/student/events', this.eventId]); },
          error: () => { this.saving = false; }
        });
      } else {
        this.eventService.createEvent(data).subscribe({
          next: (res: any) => { this.router.navigate(['/student/events', res.id]); },
          error: () => { this.saving = false; }
        });
      }
    }
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      const file = input.files[0];
      this.uploading = true;
      this.imageUploadService.uploadImage(file).subscribe({
        next: (url) => {
          this.eventForm.patchValue({ imageUrl: url });
          this.uploading = false;
        },
        error: () => { this.uploading = false; }
      });
    }
  }

  goBack() {
    if (this.clubId) {
      this.router.navigate(['/student/clubs', this.clubId]);
    } else {
      this.router.navigate(['/student/events']);
    }
  }
}

